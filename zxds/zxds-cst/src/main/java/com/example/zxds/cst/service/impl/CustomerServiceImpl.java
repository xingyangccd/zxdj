package com.example.zxds.cst.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.MicroAppUtil;
import com.example.zxds.common.util.SnowflakeUtil;
import com.example.zxds.cst.db.dao.CustomerDao;
import com.example.zxds.cst.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;

    private final MicroAppUtil microAppUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerNewCustomer(Map<String, Object> param) {
        // 1. 用微信临时凭证 code 换 openId
        String code = MapUtil.getStr(param, "code");
        String openId = microAppUtil.getOpenId(code);

        // 2. 判断这个微信是否已经注册过
        Map<String, Object> tempParam = new HashMap<>(2);
        tempParam.put("openId", openId);
        if (customerDao.hasCustomer(tempParam) != 0) {
            throw new ZxdsException("该微信无法注册");
        }

        // 3. 生成雪花 ID，写入注册数据
        long id = SnowflakeUtil.customerId();
        param.put("id", id);
        param.put("openId", openId);
        customerDao.registerNewCustomer(param);

        // 4. 回查 customerId 返回
        return customerDao.searchCustomerId(openId);
    }

    @Override
    public String login(String code) {
        String openId = microAppUtil.getOpenId(code);
        String customerId = customerDao.login(openId);
        customerId = (customerId != null ? customerId : "");
        return customerId;
    }

    @Override
    public Map<String, Object> searchCustomerInfoInOrder(long customerId) {
        return customerDao.searchCustomerInfoInOrder(customerId);
    }

    @Override
    public Map<String, Object> searchCustomerBriefInfo(long customerId) {
        return customerDao.searchCustomerBriefInfo(customerId);
    }

    @Override
    public String searchCustomerOpenId(long customerId) {
        return customerDao.searchCustomerOpenId(customerId);
    }
}