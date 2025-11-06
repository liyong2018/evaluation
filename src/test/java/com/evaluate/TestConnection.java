package com.evaluate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 简单的连接测试类
 */
public class TestConnection {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres";
        String user = "postgres.olcdeeonmpjijxtvolum";
        String password = "Htht@12#$";

        System.out.println("=== 测试Supabase连接 ===");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println("Password: " + password);

        // 方法1: 使用Properties对象
        System.out.println("\n--- 尝试Properties方法 ---");
        try {
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("ssl", "false");

            try (Connection connection = DriverManager.getConnection(url, props)) {
                if (connection.isValid(5)) {
                    System.out.println("✅ 连接成功！");

                    // 测试查询
                    try (java.sql.Statement stmt = connection.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT version()")) {
                        if (rs.next()) {
                            System.out.println("数据库版本: " + rs.getString(1));
                        }
                    }

                    // 测试表查询
                    try (java.sql.Statement stmt = connection.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery(
                             "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' LIMIT 10")) {
                        System.out.println("现有表:");
                        boolean hasTables = false;
                        while (rs.next()) {
                            System.out.println("- " + rs.getString(1));
                            hasTables = true;
                        }
                        if (!hasTables) {
                            System.out.println("(无表或权限不足)");
                        }
                    }

                    System.out.println("🎉 Supabase连接测试成功！");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 连接失败: " + e.getMessage());
            System.out.println("错误代码: " + e.getErrorCode());
            System.out.println("SQL状态: " + e.getSQLState());

            // 详细诊断
            if (e.getMessage().contains("password authentication failed")) {
                System.out.println("\n💡 密码认证失败建议:");
                System.out.println("1. 检查密码是否正确: " + password);
                System.out.println("2. 确认用户名是否正确: " + user);
                System.out.println("3. 检查Supabase Dashboard中的用户权限");
                System.out.println("4. 考虑重置密码为不含特殊字符的格式");
            }

            if (e.getMessage().contains("Connection refused")) {
                System.out.println("💡 连接被拒绝建议:");
                System.out.println("1. 检查网络连接");
                System.out.println("2. 确认Supabase项目状态");
                System.out.println("3. 检查防火墙设置");
            }

            e.printStackTrace();
        }

        System.out.println("\n=== 测试完成 ===");
    }
}