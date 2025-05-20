package com.olsonsolution.common.spring.domain.service.migration;

import com.olsonsolution.common.migration.domain.model.DomainChangeLog;
import com.olsonsolution.common.migration.domain.model.DomainSchemaConfig;
import com.olsonsolution.common.migration.domain.port.repository.ChangelogProvider;
import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;
import com.olsonsolution.common.migration.domain.port.stereotype.SchemaConfig;
import com.olsonsolution.common.spring.domain.port.config.jpa.JpaSpecConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GeneratedChangeLogProvider implements ChangelogProvider, VariablesProvider {

    private static final String MASTER_CHANGELOG_LOCATION = "classpath:/db/changelog/db.changelog-master.xml";

    private final List<JpaSpecConfig> jpaSpecConfigs;

    @Override
    public Collection<? extends ChangeLog> getChangelogs() {
        return jpaSpecConfigs.stream()
                .map(this::buildSchemaConfig)
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableMap(DefaultMapEntry::getKey, DefaultMapEntry::getValue),
                        this::buildChangeLog
                ));
    }

    @Override
    public Map<String, String> getVariables() {
        return jpaSpecConfigs.stream()
                .map(config -> new DefaultMapEntry<>(config.getJpaSpec() + "Schema", config.getSchema()))
                .collect(Collectors.toMap(DefaultMapEntry::getKey, DefaultMapEntry::getValue));
    }

    private DefaultMapEntry<String, SchemaConfig> buildSchemaConfig(JpaSpecConfig jpaSpecConfig) {
        String schema = jpaSpecConfig.getSchema();
        boolean createSchema = jpaSpecConfig.isCreateSchema();
        SchemaConfig config = DomainSchemaConfig.builder()
                .createSchema(createSchema)
                .build();
        return new DefaultMapEntry<>(schema, config);
    }

    private Collection<? extends ChangeLog> buildChangeLog(Map<String, SchemaConfig> schemasConfig) {
        return Collections.singleton(DomainChangeLog.builder()
                .schemas(schemasConfig)
                .path(MASTER_CHANGELOG_LOCATION)
                .build());
    }

}
