package com.example.zxds.common.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
/**
 * 微信小程序工具类
 * <p>
 * 作用：封装调用微信小程序服务端接口的常用方法，方便业务代码直接使用。
 * <p>
 * 主要功能：
 * 1. getOpenId(String code)：
 *    前端小程序调用 wx.login() 会拿到一个临时登录凭证 code，
 *    把它传进来，就能换取用户的唯一标识 openid。
 *    （openid 用于识别"这个用户是谁"）
 * <p>
 * 2. getAccessToken()：
 *    获取微信接口调用凭证 access_token，
 *    调用大部分微信服务端接口（如发送消息、获取用户信息）都需要带上它。
 * <p>
 * 说明：
 * - appId 和 appSecret 通过配置文件注入（@Value）；
 * - 接口返回的是 JSON，用 Hutool 的 JSONUtil 解析；
 * - 如果微信返回的结果里没有期望字段，说明调用失败，
 *   会抛出 ZxdsException 把错误信息返回给前端。
 */
@Component("MicroAppUtil")
public class MicroAppUtil {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    public String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> map = new HashMap<>();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.isEmpty()) {
            throw new ZxdsException("临时登陆凭证错误");
        }
        return openId;
    }

    public String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, Object> map = new HashMap<>();
        map.put("grant_type", "client_credential");
        map.put("appid", appId);
        map.put("secret", appSecret);
        String response = HttpUtil.get(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String accessToken = json.getStr("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new ZxdsException(json.getStr("errmsg"));
        }
        return accessToken;
    }
}