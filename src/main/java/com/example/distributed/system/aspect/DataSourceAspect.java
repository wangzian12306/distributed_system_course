package com.example.distributed.system.aspect;

import com.example.distributed.system.config.DataSourceConfig;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    // 定义切点，匹配所有Mapper接口的方法
    @Pointcut("execution(* com.example.distributed.system.mapper.*.*(..))")
    public void dataSourcePointcut() {
    }

    // 在方法执行前切换数据源
    @Before("dataSourcePointcut()")
    public void beforeAdvice(org.aspectj.lang.JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        
        // 根据方法名判断是读操作还是写操作
        if (isReadOperation(methodName)) {
            // 读操作使用从数据源
            DataSourceConfig.setDataSourceType(DataSourceConfig.DataSourceType.SLAVE);
        } else {
            // 写操作使用主数据源
            DataSourceConfig.setDataSourceType(DataSourceConfig.DataSourceType.MASTER);
        }
    }

    // 判断是否为读操作
    private boolean isReadOperation(String methodName) {
        // 常见的读操作方法名前缀
        String[] readMethods = {"find", "select", "get", "query", "list"};
        
        for (String prefix : readMethods) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}