package com.example.distributed.system.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class InstanceIdInterceptor implements HandlerInterceptor {

    private static String instanceId;

    static {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            instanceId = localHost.getHostName() + "-" + System.getProperty("server.port", "8080");
        } catch (UnknownHostException e) {
            instanceId = "unknown-" + System.getProperty("server.port", "8080");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 添加X-Instance-Id响应头
        response.addHeader("X-Instance-Id", instanceId);
        return true;
    }

    public static String getInstanceId() {
        return instanceId;
    }
}
