package com.evaluate.migration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Enable SupabaseMigrationRunner only when property migration.enabled=true
 */
public class SupabaseMigrationRunnerEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabled = context.getEnvironment().getProperty("migration.enabled", "false");
        return "true".equalsIgnoreCase(enabled);
    }
}

