package com.example.zxds.vhr.service.impl;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.vhr.db.dao.VoucherCustomerDao;
import com.example.zxds.vhr.service.VoucherCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoucherCustomerServiceImpl implements VoucherCustomerService {

    private final VoucherCustomerDao voucherCustomerDao;

    @Override
    public String useVoucher(Map<String, Object> param) {

        String discount = voucherCustomerDao.validCanUseVoucher(param);
        if(discount != null){
            // 表示代金券可用。
            int rows = voucherCustomerDao.bindVoucher(param);
            if(rows != 1){
                throw new ZxdsException("代金券不可用");
            }
            // 返回面额
            return discount;
        }
        throw new ZxdsException("代金券不可用");
    }
}
