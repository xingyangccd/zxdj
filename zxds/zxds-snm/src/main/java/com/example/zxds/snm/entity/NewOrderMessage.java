package com.example.zxds.snm.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 新订单消息
 * 一条消息推送给一个符合条件的司机，包含订单的核心信息
 */
@Data
public class NewOrderMessage implements Serializable {

    /**
     * 司机 id（消息路由键，决定推给哪个司机的队列）
     */
    private String userId;

    /**
     * 订单 id
     */
    private String orderId;

    /**
     * 上车点地址
     */
    private String from;

    /**
     * 终点地址
     */
    private String to;

    /**
     * 预估价格
     */
    private String expectsFee;

    /**
     * 预估里程（公里）
     */
    private String mileage;

    /**
     * 预估时长（分钟）
     */
    private String minute;

    /**
     * 上车点距离司机的距离（公里）
     */
    private String distance;

    /**
     * 顾客好处费（加价）
     */
    private String favourFee;

    private static final long serialVersionUID = 1L;
}