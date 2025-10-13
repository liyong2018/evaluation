package com.evaluate.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
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
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
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
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}