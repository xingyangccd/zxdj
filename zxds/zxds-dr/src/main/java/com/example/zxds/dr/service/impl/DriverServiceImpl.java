package com.example.zxds.dr.service.impl;

import cn.hutool.core.io.LimitedInputStream;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.MicroAppUtil;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.SnowflakeUtil;
import com.example.zxds.dr.db.dao.DriverDao;
import com.example.zxds.dr.db.dao.DriverSettingsDao;
import com.example.zxds.dr.db.dao.WalletDao;
import com.example.zxds.dr.db.pojo.DriverSettingsEntity;
import com.example.zxds.dr.db.pojo.WalletEntity;
import com.example.zxds.dr.service.DriverService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final MicroAppUtil microAppUtil;
    private final DriverDao driverDao;
    private final DriverSettingsDao driverSettingsDao;
    private final WalletDao walletDao;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;

    @Value("${tencent.cloud.face.groupName}")
    private String groupName;

    @Value("${tencent.cloud.face.region}")
    private String region;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerNewDriver(Map<String, Object> params) {
        // params中有临时登录码code，拿code换openId
        String code = MapUtil.getStr(params, "code");
        String openId = microAppUtil.getOpenId(code);

        // 看看司机是否注册过了
        long total = driverDao.hasDriver(Map.of("openId", openId));
        if(total != 0){
            throw new ZxdsException("该微信无法注册");
        }

        // 通过雪花算法生成司机表需要的id
        long driverId = SnowflakeUtil.driverId();
        // 组装司机的信息
        Map<String,Object> driver = new HashMap<>();
        driver.put("id", driverId);
        driver.put("openId", openId);
        driver.put("nickname", MapUtil.getStr(params, "nickname"));
        driver.put("photo", MapUtil.getStr(params, "photo"));
        driver.put("realAuth", (byte)1);
        driver.put("summary", "{\"level\":0,\"totalOrder\":0,\"weekOrder\":0,\"weekComment\":0,\"appeal\":0}");
        driver.put("archive", false);
        driver.put("status", (byte)1);
        int rows = driverDao.registerNewDriver(driver);
        if(rows != 1) {
            throw new ZxdsException("司机注册失败");
        }

        // 添加司机默认设置
        DriverSettingsEntity driverSettingsEntity = new DriverSettingsEntity();
        driverSettingsEntity.setId(SnowflakeUtil.driverId());
        driverSettingsEntity.setDriverId(driverId);
        JSONObject json = new JSONObject();
        json.set("orientation", "");
        json.set("listenService", true);
        json.set("orderDistance", 0);
        json.set("rangeDistance", 5);
        json.set("autoAccept", false);
        driverSettingsEntity.setSettings(json.toString());
        rows = driverSettingsDao.insertDriverSettings(driverSettingsEntity);
        if(rows != 1){
            throw new ZxdsException("司机注册失败");
        }

        // 添加司机钱包信息
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(SnowflakeUtil.driverId());
        walletEntity.setDriverId(driverId);
        walletEntity.setBalance(BigDecimal.ZERO);
        walletEntity.setPassword(null);
        rows = walletDao.insertWallet(walletEntity);
        if(rows != 1){
            throw new ZxdsException("司机注册失败");
        }
        // 为什么要返回一个字符串？因为底层雪花算法生成的是一个long类型的数字，前端或移动端拿到这个long类型数字可能精度损失（所以返回字符串）
        return String.valueOf(driverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDriverAuth(Map<String, Object> param) {
        int rows = driverDao.updateDriverAuth(param);
        if(rows != 1){
            throw new ZxdsException("实名认证信息更新失败");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDriverFaceModel(long driverId, String photo) {
        // 1. 查询司机的姓名和性别
        Map<String, Object> map = driverDao.searchDriverNameAndSex(driverId);
        if(map == null){
            return "司机未注册";
        }
        String name = MapUtil.getStr(map, "name");
        String sex = MapUtil.getStr(map, "sex");

        // 2.封装请求对象
        CreatePersonRequest request = new CreatePersonRequest();
        request.setGroupId(groupName);
        request.setPersonId(String.valueOf(driverId));
        request.setGender("男".equals(sex) ? 1L : 2L);
        request.setQualityControl(4L);
        request.setUniquePersonControl(4L);
        request.setPersonName(name);
        request.setImage(photo);

        try {
            // 3.调用腾讯云服务，向人员库中保存人员
            Credential cred = new Credential(secretId, secretKey);
            IaiClient client = new IaiClient(cred, region);
            CreatePersonResponse response = client.CreatePerson(request);
            String faceId = response.getFaceId();
            if(StrUtil.isBlank(faceId)) {
                return "司机面部档案创建失败，未返回FaceId";
            }
        } catch (TencentCloudSDKException e) {
            log.error("司机面部档案创建失败，driverId={}", driverId, e);
            return "司机面部档案创建失败";
        }

        // 4.更新司机表的archive字段
        int rows = driverDao.updateDriverArchive(driverId);
        if(rows != 1){
            return "更新司机归档字段失败";
        }

        // 成功的话最终返回的是一个空字符串。
        return "";
    }

    @Override
    public Map<String, Object> login(String code) {
        // 用户提交了code，通过code换openId
        String openId = microAppUtil.getOpenId(code);
        // 根据openId查询司机信息
        Map<String, Object> result = driverDao.login(openId);
        // 判断司机信息是否不为空，并且返回的司机信息里有 archive 字段
        if(result != null && result.containsKey("archive")){
            result.replace("archive", MapUtil.getInt(result, "archive") == 1);
        }
        // result对象中有什么数据？
        // id(String)、realAuth、archive
        return result;
    }

    @Override
    public Map<String, Object> searchDriverBaseInfo(long driverId) {
        // 查询司机的基本信息
        Map<String, Object> result = driverDao.searchDriverBaseInfo(driverId);
        if(result == null) {
            return null;
        }
        // summary字段从数据库中取出的是字符串，响应给前端系统应该是一个对象。
        String summaryStr = MapUtil.getStr(result, "summary");
        if(StrUtil.isNotBlank(summaryStr)) {
            result.put("summary", JSONUtil.parseObj(summaryStr));
        }
        return result;
    }

    @Override
    public PageUtils searchDriverByPage(Map<String, Object> param) {
        long total = driverDao.searchDriverCount(param);
        List<Map<String, Object>> list = new ArrayList<>();
        if(total > 0) {
            list = driverDao.searchDriverByPage(param);
        }
        Integer pageIndex = MapUtil.getInt(param, "pageIndex");
        Integer pageSize = MapUtil.getInt(param, "pageSize");

        // 创建一个通用的分页对象。
        return new PageUtils(list, total, pageIndex, pageSize);
    }

    @Override
    public Map<String, Object> searchDriverAuth(long driverId) {
        return driverDao.searchDriverAuth(driverId);
    }

    @Override
    public Map<String, Object> searchDriverRealSummary(long driverId) {
        return driverDao.searchDriverRealSummary(driverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDriverRealAuth(Map<String, Object> param) {
        return driverDao.updateDriverRealAuth(param);
    }

    @Override
    public Map<String, Object> searchDriverBriefInfo(long driverId) {
        return driverDao.searchDriverBriefInfo(driverId);
    }

    @Override
    public String searchDriverOpenId(long driverId) {
        return driverDao.searchDriverOpenId(driverId);
    }
}















