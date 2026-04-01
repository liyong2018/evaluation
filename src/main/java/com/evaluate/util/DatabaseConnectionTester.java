package com.evaluate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接测试工具
 * 使用方法：启动应用时添加 --test-connection=true 参数
 */
@Component
@ConditionalOnProperty(name = "test-connection", havingValue = "true")
public class DatabaseConnectionTester implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionTester.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${supabase.jdbc.url:}")
    private String supabaseUrl;

    @Value("${supabase.jdbc.user:postgres}")
    private String supabaseUser;

    @Value("${supabase.jdbc.password:}")
    private String supabasePassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 数据库连接测试 ===");

        // 测试当前数据源
        testConnection("当前数据源", dbUrl, dbUser, dbPassword);

        // 如果配置了Supabase，也测试一下
        if (supabaseUrl != null && !supabaseUrl.isEmpty() && !supabaseUrl.contains("your-project-ref")) {
            testConnection("Supabase", supabaseUrl, supabaseUser, supabasePassword);
        } else {
            log.warn("⚠️  Supabase配置未正确设置");
        }

        log.info("=== 测试完成 ===");
        System.exit(0);
    }

    private void testConnection(String name, String url, String user, String password) {
        log.info("测试 {} 连接...", name);
        try {
            long start = System.currentTimeMillis();
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                long end = System.currentTimeMillis();
                if (connection.isValid(5)) {
                    log.info("✅ {} 连接成功！耗时: {}ms", name, end - start);

                    // 获取数据库信息
                    try (java.sql.Statement stmt = connection.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT version()")) {
                        if (rs.next()) {
                            String version = rs.getString(1);
                            log.info("   数据库版本: {}", version);
                        }
                    }

                    // 测试表是否存在
                    try (java.sql.Statement stmt = connection.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery(
                             "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' LIMIT 5")) {
                        log.info("   现有表:");
                        while (rs.next()) {
                            log.info("   - {}", rs.getString(1));
                        }
                    }

                } else {
                    log.error("❌ {} 连接无效", name);
                }
            }
        } catch (SQLException e) {
            log.error("❌ {} 连接失败: {}", name, e.getMessage());
            log.debug("详细错误信息: ", e);
        }
    }
}