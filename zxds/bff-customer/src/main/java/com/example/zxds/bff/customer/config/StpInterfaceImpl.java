package com.example.zxds.bff.customer.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
/**
 * Sa-Token 权限认证接口实现类
 * <p>
 * 作用：告诉 Sa-Token 某个用户拥有哪些权限和哪些角色，
 * 供 @SaCheckPermission、@SaCheckRole 等注解做权限校验时使用。
 * <p>
 * 说明：当前两个方法都返回空集合，表示暂未接入具体的权限/角色数据，
 * 即所有用户的权限和角色都为空（相当于不做细粒度权限控制）。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回某个用户所拥有的权限集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginKey) {
        return Collections.emptyList();
    }

    /**
     * 返回某个用户所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginKey) {
        return Collections.emptyList();
    }

}