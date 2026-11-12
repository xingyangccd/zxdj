package com.example.zxds.mis.api.db.dao;


import com.example.zxds.mis.api.db.pojo.DeptEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface DeptDao {
    ArrayList<HashMap> searchAllDept();

    HashMap searchById(int id);

    ArrayList<HashMap> searchDeptByPage(Map param);

    long searchDeptCount(Map param);

    boolean searchCanDelete(Integer[] ids);

    int insert(DeptEntity dept);

    int update(DeptEntity dept);

    int deleteDeptByIds(Integer[] ids);
}




