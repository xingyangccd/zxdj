package com.example.zxds.dr.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.dr.db.dao.WalletDao;
import com.example.zxds.dr.db.dao.WalletIncomeDao;
import com.example.zxds.dr.db.pojo.WalletIncomeEntity;
import com.example.zxds.dr.service.WalletIncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletIncomeServiceImpl implements WalletIncomeService {

    private final WalletIncomeDao walletIncomeDao;
    private final WalletDao walletDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int transfer(WalletIncomeEntity entity, String password) {
        // 1. 判断如果是扣款（负数），强制要求密码不能为空
        if(entity.getAmount().compareTo(BigDecimal.ZERO) < 0 && StrUtil.isBlank(password)) {
            throw new ZxdsException("扣款操作必须输入支付密码");
        }

        // 2. 组装参数（给SQL语句）
        Map<String, Object> param = new HashMap<>();
        param.put("driverId", entity.getDriverId());
        param.put("amount", entity.getAmount());
        param.put("password", password);

        // 3. 先更新余额
        int rows = walletDao.updateWalletBalance(param);
        if(rows != 1){
            throw new ZxdsException("余额不足或密码错误");
        }

        // 4. 再记录流水
        rows = walletIncomeDao.insert(entity);
        if(rows != 1){
            throw new ZxdsException("添加转账记录失败");
        }
        return rows;
    }
}
