package com.example.zxds.dr.db.dao;

import com.example.zxds.dr.db.pojo.DriverSettingsEntity;

public interface DriverSettingsDao {
    int insertDriverSettings(DriverSettingsEntity entity);
    String searchDriverSettings(long driverId);
}




