package com.example.zxds.nebula.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.SqlEscapeUtils;
import com.example.zxds.nebula.db.dao.OrderMonitoringDao;
import com.example.zxds.nebula.db.dao.OrderVoiceTextDao;
import com.example.zxds.nebula.db.pojo.OrderVoiceTextEntity;
import com.example.zxds.nebula.service.MonitoringService;
import com.example.zxds.nebula.task.VoiceTextCheckTask;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    // 保存聊天文本到HBase
    private final OrderVoiceTextDao orderVoiceTextDao;

    // 保存聊天语音文件到minio服务器上
    private final MinioClient client;

    private final VoiceTextCheckTask voiceTextCheckTask;

    // 桶的名字
    private static final String BUCKET_NAME = "zxds-record";

    private final OrderMonitoringDao orderMonitoringDao;

    @Override
    public int insertOrderMonitoring(long orderId) {
        int rows = orderMonitoringDao.insert(orderId);
        if (rows != 1) {
            throw new ZxdsException("添加订单监控摘要记录失败");
        }
        return rows;
    }

    @Override
    public void monitoring(MultipartFile file, String name, String text) {
        // 1.解析订单号
        Long orderId = parseOrderId(name);
        String uuid = IdUtil.simpleUUID();

        // 2.上传录音文件到minio服务器
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(name)
                    .stream(file.getInputStream(), -1, 5 * 1024 * 1024)
                    .contentType("audio/x-mpeg").build());
        } catch (Exception e) {
            log.error("上传代驾录音文件失败，orderId={}，fileName={}", orderId, name, e);
            throw new ZxdsException("上传代驾录音文件失败");
        }

        // 3. 将录音文本保存到HBase
        OrderVoiceTextEntity entity = new OrderVoiceTextEntity();
        entity.setUuid(uuid);
        entity.setOrderId(orderId);
        entity.setRecordFile(SqlEscapeUtils.escape(name)); // 防止SQL注入
        entity.setText(SqlEscapeUtils.escape(text));

        try {
            int rows = orderVoiceTextDao.insert(entity);
            if (rows != 1) {
                throw new ZxdsException("保存录音文稿失败");
            }
        } catch (Exception e) {
            // 补偿：需要将minio上的录音文件删除。避免残留垃圾。
            log.error("保存录音文稿失败，尝试回滚Minio文件，uuid={}", uuid, e);
            try {
                client.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(name).build());
            } catch (Exception ex) {
                log.error("回滚删除minio文件失败，请人工清理：bucket={},object={}", BUCKET_NAME, name, ex);
            }
            throw new ZxdsException("保存录音文稿失败");
        }

        // 异步调用腾讯云接口对这段司乘对话文本进行安全审核，并把审核结果（标签、建议、违规词）更新回 HBase 数据库。
        voiceTextCheckTask.checkText(orderId, text, uuid);
    }

    /**
     * 解析文件名，获取订单ID。
     * 文件名可能是：订单ID-Number.mp3
     * 123123456456432123-1.mp3
     * 123123456456432123-2.mp3
     * 123123456456432123-3.mp3
     * 123123456456432123-4.mp3
     * 123123456456432123-5.mp3
     * 123123456456432123-6.mp3
     * 123123456456432123-7.mp3
     * ....
     */
    private Long parseOrderId(String name) {
        if (StrUtil.isBlank(name) || !name.contains(".mp3") || !name.contains("-")) {
            throw new ZxdsException("录音文件名格式不合法：" + name);
        }
        try {
            String[] temp = name.substring(0, name.indexOf(".mp3")).split("-");
            return Long.parseLong(temp[0]);
        } catch (Exception e) {
            log.error("解析订单号失败，fileName={}", name, e);
            throw new ZxdsException("录音文件名格式不合法：" + name);
        }
    }
}


















