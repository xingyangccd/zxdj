package com.example.zxds.vhr.db.dao;


import com.example.zxds.vhr.db.pojo.VoucherEntity;

import java.util.ArrayList;

public interface VoucherDao {
    int insert(VoucherEntity entity);

    ArrayList<String> searchIdByUUID(ArrayList<String> list);
}




