package com.example.zxds.odr.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.common.util.SnowflakeUtil;
import com.example.zxds.odr.controller.form.*;
import com.example.zxds.odr.db.pojo.OrderBillEntity;
import com.example.zxds.odr.db.pojo.OrderEntity;
import com.example.zxds.odr.service.OrderService;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/order")
@Tag(name = "OrderController", description = "订单模块Web接口")
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final NotificationConfig notificationConfig;

    @PostMapping("/driver-today-stat")
    @Operation(summary = "查询司机当天营业数据")
    public R searchDriverTodayBusinessData(@RequestBody @Valid SearchDriverTodayBusinessDataForm form) {
        Map<String, Object> map = orderService.searchDriverTodayBusinessData(form.getDriverId());
        return R.ok().put("result", map);
    }

    @PostMapping
    @Operation(summary = "顾客下单")
    public R insertOrder(@RequestBody @Valid InsertOrderForm form) {
        // 组装OrderEntity以及OrderBillEntity
        // ========== 1. 组装订单实体 ==========
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(SnowflakeUtil.orderId());
        orderEntity.setUuid(form.getUuid());
        orderEntity.setCustomerId(form.getCustomerId());
        orderEntity.setStartPlace(form.getStartPlace());

        // 起点坐标 → JSON
        JSONObject startJson = new JSONObject();
        startJson.set("latitude", form.getStartPlaceLatitude());
        startJson.set("longitude", form.getStartPlaceLongitude());
        orderEntity.setStartPlaceLocation(startJson.toString());

        orderEntity.setEndPlace(form.getEndPlace());

        // 终点坐标 → JSON
        JSONObject endJson = new JSONObject();
        endJson.set("latitude", form.getEndPlaceLatitude());
        endJson.set("longitude", form.getEndPlaceLongitude());
        orderEntity.setEndPlaceLocation(endJson.toString());

        orderEntity.setExpectsMileage(new BigDecimal(form.getExpectsMileage()));
        orderEntity.setExpectsFee(new BigDecimal(form.getExpectsFee()));
        orderEntity.setFavourFee(new BigDecimal(form.getFavourFee()));
        orderEntity.setChargeRuleId(form.getChargeRuleId());
        orderEntity.setCarPlate(form.getCarPlate());
        orderEntity.setCarType(form.getCarType());
        orderEntity.setDate(form.getDate());

        // ========== 2. 组装账单实体 ==========
        OrderBillEntity billEntity = new OrderBillEntity();
        billEntity.setId(SnowflakeUtil.orderId());
        billEntity.setBaseMileage(form.getBaseMileage());
        billEntity.setBaseMileagePrice(new BigDecimal(form.getBaseMileagePrice()));
        billEntity.setExceedMileagePrice(new BigDecimal(form.getExceedMileagePrice()));
        billEntity.setBaseMinute(form.getBaseMinute());
        billEntity.setExceedMinutePrice(new BigDecimal(form.getExceedMinutePrice()));
        billEntity.setBaseReturnMileage(form.getBaseReturnMileage());
        billEntity.setExceedReturnPrice(new BigDecimal(form.getExceedReturnPrice()));

        // ========== 3. 保存 ==========
        String id = orderService.insertOrder(orderEntity, billEntity);
        return R.ok().put("result", id);
    }

    @PutMapping("/driver")
    @Operation(summary = "司机接单")
    public R acceptNewOrder(@RequestBody @Valid AcceptNewOrderForm form) {
        // result是：接单成功、抢单失败.....
        String result = orderService.acceptNewOrder(form.getDriverId(), form.getOrderId());
        return R.ok().put("result", result);
    }

    @GetMapping("/driver/execute-order")
    @Operation(summary = "查询司机正在执行的订单记录")
    public R searchDriverExecuteOrder(@Valid SearchDriverExecuteOrderForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Map<String, Object> map = orderService.searchDriverExecuteOrder(param);
        return R.ok().put("result", map);
    }

    @GetMapping("/status")
    @Operation(summary = "查询订单状态")
    public R searchOrderStatus(@Valid SearchOrderStatusForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Integer status = orderService.searchOrderStatus(param);
        return R.ok().put("result", status);
    }

    @DeleteMapping("/unaccepted")
    @Operation(summary = "删除没有司机接单的订单")
    public R deleteUnAcceptOrder(@RequestBody @Valid DeleteUnAcceptOrderForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String result = orderService.deleteUnAcceptOrder(param);
        return R.ok().put("result", result);
    }

    @GetMapping("/driver/current")
    @Operation(summary = "查询司机当前订单")
    public R searchDriverCurrentOrder(@Valid SearchDriverCurrentOrderForm form) {
        Map<String, Object> map = orderService.searchDriverCurrentOrder(form.getDriverId());
        return R.ok().put("result", map);
    }

    @GetMapping("/customer/current")
    @Operation(summary = "查询乘客是否存在当前的订单")
    public R hasCustomerCurrentOrder(@Valid HasCustomerCurrentOrderForm form) {
        Map<String, Object> map = orderService.hasCustomerCurrentOrder(form.getCustomerId());
        return R.ok().put("result", map);
    }

    @GetMapping("/move-info")
    @Operation(summary = "查询订单信息用于司乘同显功能")
    public R searchOrderForMoveById(@Valid SearchOrderForMoveByIdForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Map<String, Object> map = orderService.searchOrderForMoveById(param);
        return R.ok().put("result", map);
    }

    @PutMapping("/arrive")
    @Operation(summary = "司机到达上车点")
    public R arriveStartPlace(@RequestBody @Valid ArriveStartPlaceForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("status", 3);
        int rows = orderService.arriveStartPlace(param);
        return R.ok().put("result", rows);
    }

    @PutMapping("/confirm-arrive")
    @Operation(summary = "乘客确认司机到达上车点")
    public R confirmArriveStartPlace(@RequestBody @Valid ConfirmArriveStartPlaceForm form) {
        boolean result = orderService.confirmArriveStartPlace(form.getOrderId());
        return R.ok().put("result", result);
    }

    @PutMapping("/start-driving")
    @Operation(summary = "开始代驾")
    public R startDriving(@RequestBody @Valid StartDrivingForm form) {
        Map<String, Object> map = BeanUtil.beanToMap(form);
        map.put("status", 4);
        int rows = orderService.startDriving(map);
        return R.ok().put("rows", rows);
    }

    @PutMapping("/status")
    @Operation(summary = "更新订单状态")
    public R updateOrderStatus(@RequestBody @Valid UpdateOrderStatusForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = orderService.updateOrderStatus(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/page")
    @Operation(summary = "查询订单分页记录")
    public R searchOrderByPage(@RequestBody @Valid SearchOrderByPageForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int pageIndex = form.getPageIndex();
        int pageSize = form.getPageSize();
        int startIndex = (pageIndex - 1) * pageSize;
        param.put("startIndex", startIndex);
        PageUtils pageUtils = orderService.searchOrderByPage(param);
        return R.ok().put("result", pageUtils);
    }

    @GetMapping("/content")
    @Operation(summary = "查询订单详情")
    public R searchOrderContent(@Valid SearchOrderContentForm form) {
        Map<String, Object> map = orderService.searchOrderContent(form.getOrderId());
        return R.ok().put("result", map);
    }

    @GetMapping("/valid-driver")
    @Operation(summary = "查询司机是否关联某订单")
    public R validDriverOwnOrder(@Valid ValidDriverOwnOrderForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        boolean bool = orderService.validDriverOwnOrder(param);
        return R.ok().put("result", bool);
    }

    @GetMapping("/settlement-data")
    @Operation(summary = "查询订单结算所需的基础数据")
    public R searchSettlementNeedData(@Valid SearchSettlementNeedDataForm form) {
        Map<String, Object> map = orderService.searchSettlementNeedData(form.getOrderId());
        return R.ok().put("result", map);
    }

    @GetMapping
    @Operation(summary = "根据ID查询订单信息")
    public R searchOrderById(@Valid SearchOrderByIdForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        // map 账单和订单的详细信息。
        Map<String, Object> map = orderService.searchOrderById(param);
        return R.ok().put("result", map);
    }

    @GetMapping("/payable")
    @Operation(summary = "验证该订单是否能够支付")
    public R validCanPayOrder(@Valid ValidCanPayOrderForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Map<String, Object> map = orderService.validCanPayOrder(param);
        return R.ok().put("result", map);
    }

    @PutMapping("/prepay-id")
    @Operation(summary = "更新订单预支付ID")
    public R updateOrderPrepayId(@RequestBody @Valid UpdateOrderPrepayIdForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = orderService.updateOrderPrepayId(param);
        return R.ok().put("rows", rows);
    }

    // 给微信支付平台提供回调的接口:notify_url，微信支付平台通过该接口告诉我们乘客支付成功了。
    @PostMapping("/wxpay-notify")
    @Operation(summary = "微信支付成功回调通知")
    public ResponseEntity<String> notify(@RequestBody String body, HttpServletRequest request) {
        log.info("【微信支付回调】收到微信支付结果通知");
        try {
            // 1.从请求头中获取验签所需参数
            String wechatpaySerial = request.getHeader("Wechatpay-Serial");
            String wechatpayNonce = request.getHeader("Wechatpay-Nonce");
            String wechatpaySignature = request.getHeader("Wechatpay-Signature");
            String wechatpayTimestamp = request.getHeader("Wechatpay-Timestamp");

            // 2. 创建解析器，自动进行验签和解密
            NotificationParser parser = new NotificationParser(notificationConfig);

            // 构建请求参数
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(wechatpaySerial)
                    .nonce(wechatpayNonce)
                    .signature(wechatpaySignature)
                    .timestamp(wechatpayTimestamp)
                    .body(body)
                    .build();

            // 验签并进行解析，获取解密之后的交易数据。
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("【微信支付回调】验签与解密成功，微信支付订单号：{}", transaction.getTransactionId());

            // 3.提取业务数据
            String uuid = transaction.getOutTradeNo();
            String payId = transaction.getTransactionId();
            String driverOpenId = transaction.getAttach(); // 因为要给司机分账，所以要获取司机的openId
            String payTime = transaction.getSuccessTime();

            // 4.验证是不是真的支付成功了。
            if("SUCCESS".equals(transaction.getTradeState().name())){
                log.info("【微信支付回调】支付成功，准备处理业务，商户订单号：{}，支付金额：{}分，", uuid, transaction.getAmount().getTotal());

                // 修改订单状态、发送系统的奖励、执行分账
                orderService.handlePayment(uuid, payId, driverOpenId, payTime);

                log.info("【微信支付回调】业务处理成功，订单号：{}", uuid);
            }else{
                log.warn("【微信支付回调】交易状态非SUCCESS，当前状态：{}，订单号：{}", transaction.getTradeState().name(), uuid);
            }

            // 5.处理成功，必须返回HTTP 200
            log.info("【微信支付回调】向微信响应处理成功（HTTP 200）");
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            log.error("【微信支付回调】处理失败，微信将重试发送，请求体原文：{}", body, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-status-sync")
    @Operation(summary = "主动查询付款结果及同步订单状态")
    public R updateOrderAboutPayment(@RequestBody @Valid UpdateOrderAboutPaymentForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String result = orderService.updateOrderAboutPayment(param);
        return R.ok().put("result", result);
    }

}



















