package com.evaluate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Supabase连接测试配置
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
public class SupabaseTestConfig {

    private static final Logger log = LoggerFactory.getLogger(SupabaseTestConfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public boolean testSupabaseConnection() {
        log.info("测试Supabase数据库连接...");
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            if (connection.isValid(5)) {
                log.info("✅ Supabase连接成功！");

                // 测试查询
                try (java.sql.Statement stmt = connection.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT version()")) {
                    if (rs.next()) {
                        String version = rs.getString(1);
                        log.info("数据库版本: {}", version);
                    }
                }

                return true;
            } else {
                log.error("❌ Supabase连接无效");
                return false;
            }
        } catch (SQLException e) {
            log.error("❌ Supabase连接失败: {}", e.getMessage(), e);
            return false;
        }
    }
}