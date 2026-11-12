package com.example.zxds.nebula.service;

import org.springframework.web.multipart.MultipartFile;

public interface MonitoringService {
    void monitoring(MultipartFile file, String name, String text);
    int insertOrderMonitoring(long orderId);
}