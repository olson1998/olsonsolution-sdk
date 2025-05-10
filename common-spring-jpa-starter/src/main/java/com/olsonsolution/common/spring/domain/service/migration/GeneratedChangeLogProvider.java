package com.olsonsolution.common.spring.domain.service.migration;

import com.olsonsolution.common.migration.domain.model.DomainChangeLog;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.spring.domain.port.config.jpa.JpaSpecConfig;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class GeneratedChangeLogProvider implements ChangelogProvider {

    private final List<JpaSpecConfig> jpaSpecConfigs;

    @Override
    public Collection<? extends ChangeLog> getChangelogs() {
        return jpaSpecConfigs.stream()
                .map(this::getGeneratedChangeLogs)
                .toList();
    }

    private ChangeLog getGeneratedChangeLogs(JpaSpecConfig jpaSpecConfig) {
        return DomainChangeLog.builder()
                .schema(jpaSpecConfig.getSchema())
                .createSchema(true)
                .path("classpath:./db/changelog/%s/db.changelog.master-changelog.xml".
                        formatted(jpaSpecConfig.getJpaSpec()))
                .build();
    }

}
