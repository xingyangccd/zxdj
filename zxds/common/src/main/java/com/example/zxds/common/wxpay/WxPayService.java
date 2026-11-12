package com.example.zxds.common.wxpay;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.example.zxds.common.exception.ZxdsException;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.profitsharing.model.CreateOrderReceiver;
import com.wechat.pay.java.service.profitsharing.model.CreateOrderRequest;
import com.wechat.pay.java.service.profitsharing.model.OrdersEntity;
import com.wechat.pay.java.service.profitsharing.model.QueryOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付的核心业务类：
 * 1.下单
 * 2.查单
 * 3.关单
 * ...
 */
@Service
public class WxPayService {

    // 注入它的目的是为了调用这个对象中的 getPrivateKeyContent() 方法。
    private final WxPayV3Config wxPayV3Config;

    // Java SDK（微信平台提供的核心对象）
    // 微信官方Java SDK中专门负责处理小程序支付业务的核心服务类。
    private final JsapiService jsapiService;

    // 注入新的Java SDK，专门用于调用分账方法。
    private final ProfitsharingService profitsharingService;

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.mch-id}")
    private String mchId;

    // 这个构造方法是需要手动定制的。
    public WxPayService(Config wxPayConfig, WxPayV3Config wxPayV3Config) {
        this.wxPayV3Config = wxPayV3Config;
        // 这里就印证了：需要在IoC容器中配置一个 Config Bean，这样才能初始化Java SDK。
        this.jsapiService = new JsapiService.Builder().config(wxPayConfig).build();
        this.profitsharingService = new ProfitsharingService.Builder().config(wxPayConfig).build();
    }

    /**
     * 小程序支付下单方法（调用这个方法完成下单：创建微信支付，生成prepay_id）
     * @param outTradeNo 商户内部给订单生成的一个订单号。
     * @param description 商品描述
     * @param totalFen 总金额，单位分
     * @param openId 支付人的id
     * @param attach 附加数据
     * @return 返回拉起支付所需要的参数
     */
    public Map<String, String> jsapiPrepay(String outTradeNo, String description, int totalFen, String openId, String attach){
        // 使用微信支付的Java SDK完成下单功能。
        // 创建请求对象，封装请求参数
        PrepayRequest request = new PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(outTradeNo);
        // 回调Url
        request.setNotifyUrl("https://s3.v100.vip:20746/zxds-odr/order/wxpay-notify");
        request.setAttach(attach);
        // 金额
        Amount amount = new Amount();
        amount.setTotal(totalFen);
        request.setAmount(amount);
        // 支付者
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        // 分账标识
        SettleInfo settleInfo = new SettleInfo();
        settleInfo.setProfitSharing(true); // 要分账
        request.setSettleInfo(settleInfo);

        // 调用微信SDK下单API创建微信支付单
        PrepayResponse response = jsapiService.prepay(request);
        // 获取预支付订单ID
        String prepayId = response.getPrepayId();

        // 组装参数，返回给前端小程序，前端小程序要提交这些参数拉起微信支付。
        // 参数怎么组装，不是随意的，因为将来要通过这些参数拉起微信支付，这个就需要参考官方文档了，找 小程序调起微信支付的文档
        // https://pay.weixin.qq.com/doc/v3/merchant/4012791898
        Map<String, String> payParams = new HashMap<>();
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.fastSimpleUUID();
        String packageStr = "prepay_id=" + prepayId;
        payParams.put("appId", appId);
        payParams.put("timeStamp", timeStamp);
        payParams.put("nonceStr", nonceStr);
        payParams.put("package", packageStr);
        payParams.put("signType", "RSA");

        // 生成 paySign（小程序端没有提供API生成签名，我们只能在后端手动拼接签名，拼完之后传送给前端小程序）
        // 小程序调起支付签名，需要参考微信官方文档：https://pay.weixin.qq.com/doc/v3/merchant/4012365341
        // 格式不能乱拼，必须符合官方格式要求。
        String signStr = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageStr + "\n";
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, wxPayV3Config.getPrivateKeyContent(), null);
        byte[] signBytes = sign.sign(signStr.getBytes(StandardCharsets.UTF_8));
        payParams.put("paySign", Base64.encode(signBytes));

        return payParams;
    }

    /**
     * 根据微信订单号查询
     */
    public Transaction queryOrderByTransactionId(String transactionId){
        // 封装请求对象
        QueryOrderByIdRequest request = new QueryOrderByIdRequest();
        request.setMchid(mchId);
        request.setTransactionId(transactionId);

        try {
            // 调用SDK方法来查询订单
            return jsapiService.queryOrderById(request);
        } catch (ServiceException e) {
            throw new ZxdsException("查询订单失败：" + e.getErrorMessage(), e);
        }
    }

    /**
     * 根据商户自己生成的订单号进行查询
     */
    public Transaction queryOrderByOutTradeNo(String outTradeNo){
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(mchId);
        request.setOutTradeNo(outTradeNo);
        try {
            // 调用SDK方法来查询订单
            return jsapiService.queryOrderByOutTradeNo(request);
        } catch (ServiceException e) {
            throw new ZxdsException("查询订单失败：" + e.getErrorMessage(), e);
        }
    }

    /**
     * 关闭订单
     */
    public void closeOrder(String outTradeNo){
        CloseOrderRequest request = new CloseOrderRequest();
        request.setMchid(mchId);
        request.setOutTradeNo(outTradeNo);

        try {
            jsapiService.closeOrder(request);
        } catch (ServiceException e) {
            throw new ZxdsException("关闭订单失败：" + e.getErrorMessage(), e);
        }
    }


    /**
     * 请求微信支付平台进行分账操作（分账操作也等于是创建一个订单。）
     * @param transactionId 微信支付订单号
     * @param outOrderNo 商户分账单号
     * @param openId 司机openId
     * @param amountFen 分账金额（分）
     * @return 微信返回的分账状态：FINISHED 或 PROCESSING
     */
    public String requestProfitSharing(String transactionId, String outOrderNo, String openId, Long amountFen){
        // 1.构建请求参数
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAppid(appId);
        request.setTransactionId(transactionId);
        request.setOutOrderNo(outOrderNo);
        request.setUnfreezeUnsplit(true); // 单次分账，分完之后剩下的余额解冻回商户。

        // 构建接收方的信息。
        CreateOrderReceiver receiver = new CreateOrderReceiver();
        receiver.setType("PERSONAL_OPENID");
        receiver.setAccount(openId);
        receiver.setAmount(amountFen);
        receiver.setDescription("代驾司机分账");
        request.setReceivers(Collections.singletonList(receiver));

        // 2.发送请求给微信平台进行分账操作。
        try {
            // 分账的核心方法
            OrdersEntity response = profitsharingService.createOrder(request);
            // 返回分账单状态：PROCESSING/FINISHED
            return response.getState().name();
        } catch (ServiceException e) {
            throw new ZxdsException("微信分账请求失败：" + e.getErrorMessage(), e);
        }
    }

    /**
     * 查询分账结果的方法。
     * @param transactionId 微信平台支付ID。
     * @param outOrderNo 分账单单号（商户内部的）
     * @return PROCESSING / FINISHED
     */
    public String queryProfitSharing(String transactionId, String outOrderNo) {
        QueryOrderRequest request = new QueryOrderRequest();
        request.setTransactionId(transactionId);
        request.setOutOrderNo(outOrderNo);
        try {
            OrdersEntity response = profitsharingService.queryOrder(request);
            // 返回分账单状态：PROCESSING / FINISHED
            return response.getState().name();
        } catch (ServiceException e) {
            throw new RuntimeException("查询分账结果失败: " + e.getErrorMessage(), e);
        }
    }


}



















