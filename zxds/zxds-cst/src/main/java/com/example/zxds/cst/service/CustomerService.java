package com.example.zxds.cst.service;

import java.util.Map;

public interface CustomerService {
    String registerNewCustomer(Map<String, Object> param);
    String login(String code);
    Map<String, Object> searchCustomerInfoInOrder(long customerId);
    Map<String, Object> searchCustomerBriefInfo(long customerId);
    String searchCustomerOpenId(long customerId);
}