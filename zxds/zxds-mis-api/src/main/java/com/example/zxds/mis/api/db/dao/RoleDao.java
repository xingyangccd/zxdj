package com.example.zxds.mis.api.db.dao;


import com.example.zxds.mis.api.db.pojo.RoleEntity;

import java.util.ArrayList;
import java.util.HashMap;

public interface RoleDao {
    ArrayList<HashMap> searchAllRole();

    HashMap searchById(int id);

    ArrayList<HashMap> searchRoleByPage(HashMap param);

    long searchRoleCount(HashMap param);

    int insert(RoleEntity role);

    ArrayList<Integer> searchUserIdByRoleId(int roleId);

    int update(RoleEntity role);

    boolean searchCanDelete(Integer[] ids);

    int deleteRoleByIds(Integer[] ids);
}




