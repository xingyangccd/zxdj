package com.example.zxds.common.wxpay;

import cn.hutool.core.io.FileUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class WxPayV3Config {

    @Value("${wx.mch-id}")
    private String mchId;

    @Value("${wx.private-key-path}")
    private String privateKeyPath;

    @Value("${wx.merchant-serial-number}")
    private String merchantSerialNumber;

    @Value("${wx.api-v3-key}")
    private String apiV3Key;

    @Bean
    public RSAAutoCertificateConfig wxPayConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }

    /**
     * 读取私钥字符串内容（用于给小程序前端生成 paySign 签名）
     */
    public String getPrivateKeyContent() {
        String content = FileUtil.readUtf8String(privateKeyPath);
        return content.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");
    }
}