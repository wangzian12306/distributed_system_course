package com.example.distributed.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    // 主数据源（写操作）
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 从数据源（读操作）
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 动态数据源
    @Bean(name = "dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(DataSource masterDataSource, DataSource slaveDataSource) {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String dataSource = DataSourceContextHolder.getDataSource();
                return dataSource != null ? dataSource : DataSourceNames.MASTER;
            }
        };

        // 配置数据源映射
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceNames.MASTER, masterDataSource);
        dataSourceMap.put(DataSourceNames.SLAVE, slaveDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);

        // 设置默认数据源
        routingDataSource.setDefaultTargetDataSource(masterDataSource);

        return routingDataSource;
    }
}