package com.example.zxds.cst.service.impl;

import com.example.zxds.common.util.SnowflakeUtil;
import com.example.zxds.cst.db.dao.CustomerCarDao;
import com.example.zxds.cst.db.pojo.CustomerCarEntity;
import com.example.zxds.cst.service.CustomerCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerCarServiceImpl implements CustomerCarService {
    private final CustomerCarDao customerCarDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertCustomerCar(CustomerCarEntity entity) {
        entity.setId(SnowflakeUtil.customerId());
        customerCarDao.insert(entity);
    }

    @Override
    public List<Map<String, Object>> searchCustomerCarList(long customerId) {
        return customerCarDao.searchCustomerCarList(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCustomerCarById(long id) {
        return customerCarDao.deleteCustomerCarById(id);
    }
}
