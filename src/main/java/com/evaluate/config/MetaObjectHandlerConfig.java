package com.evaluate.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 字段自动填充处理器
 *
 * @author System
 * @since 2024-01-01
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间 - 支持两种命名: createTime 和 createdTime
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间 - 支持两种命名: updateTime 和 updatedTime
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

        // 自动填充创建人
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            this.strictInsertFill(metaObject, "createdBy", String.class, currentUsername);
            this.strictInsertFill(metaObject, "updatedBy", String.class, currentUsername);
        }

        // 为 FormulaConfig 的 algorithmStepId 字段设置默认值
        if (metaObject.getOriginalObject().getClass().getSimpleName().equals("FormulaConfig")) {
            Object algorithmStepId = getFieldValByName("algorithmStepId", metaObject);
            if (algorithmStepId == null || "".equals(algorithmStepId)) {
                this.strictInsertFill(metaObject, "algorithmStepId", String.class, null);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间 - 支持两种命名: updateTime 和 updatedTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

        // 自动填充更新人
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // 如果获取失败，返回 null 或 "system"
            return "system";
        }
        return "system";
    }
}