package com.example.zxds.dr.db.dao;


import com.example.zxds.dr.db.pojo.WalletEntity;

import java.util.Map;

public interface WalletDao {
    int insertWallet(WalletEntity entity);

    int updateWalletBalance(Map<String, Object> param);
}