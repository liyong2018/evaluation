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
                .url("jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres")
                .username("postgres.olcdeeonmpjijxtvolum")
                .password("Htht@1234")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}