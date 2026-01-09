package com.evaluate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.sql.DataSource;

/**
 * Supabase数据源配置
 * 专门处理特殊字符密码
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
public class SupabaseDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "supabase.jdbc")
    public DataSource supabaseDataSource() {
        return DataSourceBuilder.create()
                .build();
    }
}
