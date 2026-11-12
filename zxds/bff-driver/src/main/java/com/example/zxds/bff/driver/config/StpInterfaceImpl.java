package com.example.zxds.bff.driver.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回某个用户所拥有的权限集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginKey) {
        // 本项目不需要用到权限判定，所以这里就返回一个空的
        return Collections.emptyList();
    }

    /**
     * 返回某个用户所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginKey) {
        // 本项目不需要用到权限判定，所以这里就返回一个空的
        return Collections.emptyList();
    }

}