package com.example.zxds.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 雪花算法工具类
 * <p>
 * 按微服务模块划分，每个模块一个独立的 Snowflake 实例，
 * workerId 各不相同，保证全局 ID 不重复。
 * <p>
 * 模块与 workerId 对应关系：
 *   司机模块       8001  ->  1
 *   订单模块       8002  ->  2
 *   消息模块       8003  ->  3
 *   地图模块       8004  ->  4
 *   订单执行模块   8005  ->  5
 *   规则模块       8006  ->  6
 *   乘客模块       8007  ->  7
 *   代金券模块     8008  ->  8
 *   私有云模块     8009  ->  9
 *   MIS模块        8010  ->  10
 *   司机BFF        8101  ->  11
 *   乘客BFF        8102  ->  12
 *   网关           8201  ->  13
 */
public class SnowflakeUtil {

    /** datacenterId 统一为 1 */
    private static final long DC_ID = 1L;

    /** 司机模块 */
    private static final Snowflake DRIVER        = IdUtil.getSnowflake(1, DC_ID);

    /** 订单模块 */
    private static final Snowflake ORDER         = IdUtil.getSnowflake(2, DC_ID);

    /** 消息模块 */
    private static final Snowflake MESSAGE       = IdUtil.getSnowflake(3, DC_ID);

    /** 地图模块 */
    private static final Snowflake MAP           = IdUtil.getSnowflake(4, DC_ID);

    /** 订单执行模块 */
    private static final Snowflake ORDER_EXEC    = IdUtil.getSnowflake(5, DC_ID);

    /** 规则模块 */
    private static final Snowflake RULE          = IdUtil.getSnowflake(6, DC_ID);

    /** 乘客模块 */
    private static final Snowflake CUSTOMER      = IdUtil.getSnowflake(7, DC_ID);

    /** 代金券模块 */
    private static final Snowflake COUPON        = IdUtil.getSnowflake(8, DC_ID);

    /** 私有云模块 */
    private static final Snowflake PRIVATE_CLOUD = IdUtil.getSnowflake(9, DC_ID);

    /** MIS模块 */
    private static final Snowflake MIS           = IdUtil.getSnowflake(10, DC_ID);

    /** 司机BFF */
    private static final Snowflake DRIVER_BFF    = IdUtil.getSnowflake(11, DC_ID);

    /** 乘客BFF */
    private static final Snowflake CUSTOMER_BFF  = IdUtil.getSnowflake(12, DC_ID);

    /** 网关 */
    private static final Snowflake GATEWAY       = IdUtil.getSnowflake(13, DC_ID);

    private SnowflakeUtil() {
    }

    /** 司机模块 ID */
    public static long driverId() {
        return DRIVER.nextId();
    }

    /** 订单模块 ID */
    public static long orderId() {
        return ORDER.nextId();
    }

    /** 消息模块 ID */
    public static long messageId() {
        return MESSAGE.nextId();
    }

    /** 地图模块 ID */
    public static long mapId() {
        return MAP.nextId();
    }

    /** 订单执行模块 ID */
    public static long orderExecId() {
        return ORDER_EXEC.nextId();
    }

    /** 规则模块 ID */
    public static long ruleId() {
        return RULE.nextId();
    }

    /** 乘客模块 ID */
    public static long customerId() {
        return CUSTOMER.nextId();
    }

    /** 代金券模块 ID */
    public static long couponId() {
        return COUPON.nextId();
    }

    /** 私有云模块 ID */
    public static long privateCloudId() {
        return PRIVATE_CLOUD.nextId();
    }

    /** MIS模块 ID */
    public static long misId() {
        return MIS.nextId();
    }

    /** 司机BFF ID */
    public static long driverBffId() {
        return DRIVER_BFF.nextId();
    }

    /** 乘客BFF ID */
    public static long customerBffId() {
        return CUSTOMER_BFF.nextId();
    }

    /** 网关 ID */
    public static long gatewayId() {
        return GATEWAY.nextId();
    }
}