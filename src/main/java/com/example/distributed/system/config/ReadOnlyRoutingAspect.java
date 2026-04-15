package com.example.distributed.system.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class ReadOnlyRoutingAspect {

    @Around("@annotation(com.example.distributed.system.config.ReadOnly)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 设置从数据源
            DataSourceContextHolder.setDataSource(DataSourceNames.SLAVE);
            return joinPoint.proceed();
        } finally {
            // 清除数据源
            DataSourceContextHolder.clearDataSource();
        }
    }
}
