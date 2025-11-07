package com.evaluate.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 支持MySQL和PostgreSQL数据库自动适配
 *
 * @author System
 * @since 2024-01-01
 */
@Configuration
public class MybatisPlusConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 分页插件 - 自动适配数据库类型
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 根据数据库类型添加分页插件
        DbType dbType = getDatabaseType();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));

        return interceptor;
    }

    /**
     * 获取数据库类型
     */
    private DbType getDatabaseType() {
        if (driverClassName != null) {
            if (driverClassName.contains("postgresql")) {
                return DbType.POSTGRE_SQL;
            } else if (driverClassName.contains("mysql")) {
                return DbType.MYSQL;
            }
        }
        return DbType.MYSQL; // 默认MySQL
    }

    /**
     * Supabase配置验证
     */
    @Configuration
    @ConditionalOnProperty(name = "migration.enabled", havingValue = "true")
    public static class SupabaseMigrationConfig {

        @Value("${supabase.jdbc.url}")
        private String supabaseUrl;

        @Value("${supabase.jdbc.password}")
        private String supabasePassword;

        @Bean
        public boolean validateSupabaseConfig() {
            if (supabaseUrl == null || supabaseUrl.contains("your-project-ref")) {
                throw new IllegalStateException("请配置正确的Supabase连接参数 in application.yml");
            }
            if (supabasePassword == null || supabasePassword.contains("your-supabase-password")) {
                throw new IllegalStateException("请配置正确的Supabase密码 in application.yml");
            }
            return true;
        }
    }
}