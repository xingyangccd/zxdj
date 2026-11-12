package com.example.zxds.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingResponse;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 腾讯云 COS（对象存储）工具类
 * <p>
 * 作用：封装文件上传、下载、删除等操作，方便业务代码直接调用。
 * <p>
 * 项目里用到了两个存储桶：
 * - 公有桶（public）：文件可以直接通过 URL 访问，适合头像、商品图等公开资源；
 * - 私有桶（private）：文件不能直接访问，需要通过临时签名 URL 访问，适合合同、证件等敏感文件。
 * <p>
 * 主要功能：
 * 1. uploadPublicFile：上传文件到公有桶，返回文件的访问 URL 和存储路径；
 * 2. uploadPrivateFile：上传文件到私有桶，只返回存储路径（需另外生成临时 URL）；
 * 3. getPrivateFileUrl：为私有桶文件生成一个 5 分钟有效的临时访问链接；
 * 4. deletePublicFile / deletePrivateFile：批量删除文件。
 * <p>
 * 额外特性：
 * - 上传的文件名会自动拼接 UUID，避免同名文件互相覆盖；
 * - 如果是图片类型（jpg/jpeg/png/gif/bmp），上传后会自动调用
 *   腾讯云内容审核接口，检测是否包含色情、暴恐、政治敏感、广告等违规内容，
 *   一旦发现违规会立即删除文件并抛出 ZxdsException("图片内容不合规")。
 */
@Component
public class CosUtil {

    private static final Set<String> IMAGE_TYPES = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp");
    private static final String AUDIT_DETECT_TYPE = "porn,terrorist,politics,ads";

    @Value("${tencent.cloud.appId}")
    private String appId;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.region-public}")
    private String regionPublic;

    @Value("${tencent.cloud.bucket-public}")
    private String bucketPublic;

    @Value("${tencent.cloud.region-private}")
    private String regionPrivate;

    @Value("${tencent.cloud.bucket-private}")
    private String bucketPrivate;

    private COSClient getCosPublicClient() {
        return buildClient(regionPublic);
    }

    private COSClient getCosPrivateClient() {
        return buildClient(regionPrivate);
    }

    private COSClient buildClient(String region) {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred, clientConfig);
    }

    /**
     * 向公有存储桶上传文件
     */
    public Map<String, Object> uploadPublicFile(MultipartFile file, String path) throws IOException {
        // 获取文件的原始名字
        String fileName = file.getOriginalFilename();
        // 获取文件的扩展名 ".jpg"
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        // 拼接存储到COS桶当中的路径
        // /driver/auth/FD123Q1R2FD3SA1F23DSA12FD1S2A3F12DSA312F3DS.jpg
        path += IdUtil.simpleUUID() + fileType;

        // 对象元信息（图片的元信息）
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());

        COSClient client = getCosPublicClient();
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPublic, path, in, meta);
            putObjectRequest.setStorageClass(StorageClass.Standard);
            client.putObject(putObjectRequest);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("url", "https://" + bucketPublic + ".cos." + regionPublic + ".myqcloud.com" + path);
        map.put("path", path);

        if (IMAGE_TYPES.contains(fileType)) { // 审核图片内容
            ImageAuditingRequest request = new ImageAuditingRequest();
            request.setBucketName(bucketPublic);
            request.setDetectType(AUDIT_DETECT_TYPE);
            request.setObjectKey(path);
            ImageAuditingResponse response = client.imageAuditing(request);
            if (isIllegal(response)) {
                // 删除文件
                client.deleteObject(bucketPublic, path);
                // 抛异常
                throw new ZxdsException("图片内容不合规");
            }
        }
        client.shutdown();
        return map;
    }

    /**
     * 向私有存储桶上传文件
     */
    public Map<String, Object> uploadPrivateFile(MultipartFile file, String path) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        path += IdUtil.simpleUUID() + fileType;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());

        COSClient client = getCosPrivateClient();
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPrivate, path, in, meta);
            putObjectRequest.setStorageClass(StorageClass.Standard);
            client.putObject(putObjectRequest);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("path", path);

        if (IMAGE_TYPES.contains(fileType)) {
            ImageAuditingRequest request = new ImageAuditingRequest();
            request.setBucketName(bucketPrivate);
            request.setDetectType(AUDIT_DETECT_TYPE);
            request.setObjectKey(path);
            ImageAuditingResponse response = client.imageAuditing(request);
            if (isIllegal(response)) {
                client.deleteObject(bucketPrivate, path);
                throw new ZxdsException("图片内容不合规");
            }
        }
        client.shutdown();
        return map;
    }

    private boolean isIllegal(ImageAuditingResponse response) {
        return !"0".equals(response.getPornInfo().getHitFlag()) || !"0".equals(response.getTerroristInfo().getHitFlag()) || !"0".equals(response.getPoliticsInfo().getHitFlag()) || !"0".equals(response.getAdsInfo().getHitFlag());
    }

    /**
     * 获取私有读写文件的临时URL外网访问地址
     */
    public String getPrivateFileUrl(String path) {
        COSClient client = getCosPrivateClient();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketPrivate, path, HttpMethodName.GET);
        Date expiration = DateUtil.offsetMinute(new Date(), 5);
        request.setExpiration(expiration);
        URL url = client.generatePresignedUrl(request);
        client.shutdown();
        return url.toString();
    }

    /**
     * 刪除公有存储桶的文件
     */
    public void deletePublicFile(String[] paths) {
        COSClient client = getCosPublicClient();
        for (String path : paths) {
            client.deleteObject(bucketPublic, path);
        }
        client.shutdown();
    }

    /**
     * 刪除私有存储桶的文件
     */
    public void deletePrivateFile(String[] paths) {
        COSClient client = getCosPrivateClient();
        for (String path : paths) {
            client.deleteObject(bucketPrivate, path);
        }
        client.shutdown();
    }

}