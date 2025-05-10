package com.olsonsolution.common.spring.domain.service.migration;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskDecorator;

import java.util.Optional;

@RequiredArgsConstructor
public class LiquibaseMigrationTaskDecorator implements TaskDecorator {

    private final DataSourceSpecManager dataSourceSpecManager;

    @Override
    public Runnable decorate(Runnable runnable) {
        Optional<DataSourceSpec> dataSourceSpec = Optional.ofNullable(dataSourceSpecManager.getThreadLocal());
        return () -> {
            Thread thread = Thread.currentThread();
            String originalName = thread.getName();
            thread.setName("liquibase-" + thread.getId());
            try {
                dataSourceSpec.ifPresent(dataSourceSpecManager::setThreadLocal);
                runnable.run();
            } finally {
                thread.setName(originalName);
                dataSourceSpecManager.clearThreadLocal();
            }
        };
    }


}
