package com.example.zxds.nebula.task;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.util.SqlEscapeUtils;
import com.example.zxds.nebula.db.dao.OrderMonitoringDao;
import com.example.zxds.nebula.db.dao.OrderVoiceTextDao;
import com.example.zxds.nebula.db.pojo.OrderMonitoringEntity;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.auditing.AuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.SectionInfo;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class VoiceTextCheckTask {

    @Value("${tencent.cloud.appid}")
    private String appId;

    @Value("${tencent.cloud.bucket-public}")
    private String bucketPublic; // 被审核的文本自动存储到指定的存储桶当中。

    private final COSClient cosClient;
    private final OrderVoiceTextDao orderVoiceTextDao;
    private final OrderMonitoringDao orderMonitoringDao;

    /**
     * 异步方法中抛出的异常会被吞掉，因此统一采用记录日志 + return的方式。
     */
    @Async
    public void checkText(long orderId, String content, String uuid){
        // 1.审核内容，返回审核结果（调用腾讯云数据万象接口）
        String label = "Normal"; // 审核结果默认是正常的。
        String suggestion = "Pass"; // 后续的建议：默认值Pass表示通过

        // 后续建议的模板
        Map<String, String> template = Map.of("0", "Pass", "1", "Block", "2", "Review");
        List<String> keywords = new ArrayList<>();

        // 调用腾讯云数据万象接口进行内容审核
        if(StrUtil.isNotBlank(content)){
            try {
                // 内容不为空才有审核的必要。
                TextAuditingRequest request = new TextAuditingRequest();
                request.setBucketName(bucketPublic); // 设置存储桶
                request.getInput().setContent(Base64.encode(content)); // 设置要审核的文本内容
                request.getConf().setDetectContent("all"); // 所有请求都进行审核，数据万象接口要审核的全部审核：谩骂、色情...

                // 发送请求获取审核结果
                TextAuditingResponse response = cosClient.createAuditingTextJobs(request);
                AuditingJobsDetail detail = response.getJobsDetail();

                if(detail != null && "Success".equals(detail.getState())){
                    // 获取检测结果
                    label = detail.getLabel();
                    // 获取后续建议（"0" "1" "2"）
                    String result = detail.getResult();
                    suggestion = template.getOrDefault(result, "Review");
                    // 从detail对象中获取违规关键字。
                    List<SectionInfo> list = detail.getSectionList();
                    if(list != null){
                        for(SectionInfo info : list){
                            String keywords1 = info.getPornInfo().getKeywords(); // 色情
                            String keywords2 = info.getIllegalInfo().getKeywords(); // 违法
                            String keywords3 = info.getAbuseInfo().getKeywords(); // 谩骂
                            if(StrUtil.isNotBlank(keywords1)){
                                keywords.addAll(Arrays.asList(keywords1.split(",")));
                            }
                            if(StrUtil.isNotBlank(keywords2)){
                                keywords.addAll(Arrays.asList(keywords2.split(",")));
                            }
                            if(StrUtil.isNotBlank(keywords3)){
                                keywords.addAll(Arrays.asList(keywords3.split(",")));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("调用腾讯云内容审核失败，appId={}, orderId={}, uuid={}", appId, orderId, uuid, e);
                return;
            }
        }

        // 2. 根据uuid获取id
        Long id = orderVoiceTextDao.searchIdByUuid(uuid);
        if(id == null){
            log.error("没有找到代驾语音文本记录，uuid={}", uuid);
            return;
        }

        // 3.将审核结果更新到 order_voice_text 表当中。
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("label", SqlEscapeUtils.escape(label));
        param.put("suggestion", SqlEscapeUtils.escape(suggestion));
        param.put("keywords", SqlEscapeUtils.escape(ArrayUtil.join(keywords.toArray(), ",")));

        int rows = orderVoiceTextDao.updateCheckResult(param);
        if(rows != 1){
            log.error("更新内容检查结果失败，id={}", id);
            return;
        }

        // 4. 查询该订单有多少个录音文件和需要人工审核的文本。
        Map<String, Object> map = orderMonitoringDao.searchOrderRecordsAndReviews(orderId);
        if(map == null){
            log.error("没有查询到订单监控记录，orderId={}", orderId);
            return;
        }

        Long monitoringId = MapUtil.getLong(map, "id");
        Integer records = MapUtil.getInt(map, "records", 0);
        Integer reviews = MapUtil.getInt(map, "reviews", 0);

        // 5.向封装实体类，在更新监控信息。
        OrderMonitoringEntity entity = new OrderMonitoringEntity();
        entity.setId(monitoringId);
        entity.setOrderId(orderId);
        entity.setRecords(records + 1);
        if("Review".equals(suggestion)) {
            entity.setReviews(reviews + 1);
        }
        if("Block".equals(suggestion)) {
            entity.setSafety("danger");
        }

        orderMonitoringDao.updateOrderMonitoring(entity);
    }

}













