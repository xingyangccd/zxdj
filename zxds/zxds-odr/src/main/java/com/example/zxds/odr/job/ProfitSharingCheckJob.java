package com.example.zxds.odr.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.wxpay.WxPayService;
import com.example.zxds.odr.db.dao.OrderProfitsharingDao;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfitSharingCheckJob {
    private final WxPayService wxPayService;
    private final OrderProfitsharingDao orderProfitsharingDao;

    @XxlJob("profitSharingCheckHandler")
    public void checkProfitSharing() {
        // 记录日志
        XxlJobHelper.log("开始执行分账查单补偿任务...");

        // 1.查询所有状态是1，并且支付时间已经超过10分钟的分账记录。
        List<Map<String, Object>> list = orderProfitsharingDao.searchProcessingRecords(10);
        //List<Map<String, Object>> list = orderProfitsharingDao.searchProcessingRecords(1); // 测试修改为1，支付时间超过1分钟的就能查分账结果。
        if(CollectionUtil.isEmpty(list)) {
            XxlJobHelper.log("没有需要补偿的分账记录");
            return;
        }

        // 一个 record 代表一个分账记录。
        for (Map<String, Object> record : list) {
            String outOrderNo = MapUtil.getStr(record, "outOrderNo");
            String transactionId = MapUtil.getStr(record, "transactionId");
            Long profitSharingId = MapUtil.getLong(record, "profitsharingId");

            // 调用V3接口，查询分账结果。
            try {
                String state = wxPayService.queryProfitSharing(transactionId, outOrderNo);

                if("FINISHED".equals(state)) {
                    // 分账完成，更新状态为2
                    orderProfitsharingDao.updateProfitsharingStatus(profitSharingId);
                    XxlJobHelper.log("查单成功，分账单号{}已完成分账", outOrderNo);
                } else if ("PROCESSING".equals(state)) {
                    // 表示目前分账仍然正在处理中，什么都不用做，等下一次的定时任务。
                    XxlJobHelper.log("分账单号：{} 仍在处理中，等待下次查询", outOrderNo);
                } else {
                    XxlJobHelper.log("分账单号：{} 状态异常：{}", outOrderNo, state);
                }
            }catch (Exception e) {
                log.error("分账查单异常，分账单号：{}", outOrderNo, e);
                XxlJobHelper.log("查单异常：{}", e.getMessage());
            }
        }
    }
}


















