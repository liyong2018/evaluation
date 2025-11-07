package com.evaluate.migration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Enable FullMigrationRunner when property migration.full.enabled=true
 */
public class FullMigrationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("migration.full.enabled", "false");
        return "true".equalsIgnoreCase(enabled);
    }
}
