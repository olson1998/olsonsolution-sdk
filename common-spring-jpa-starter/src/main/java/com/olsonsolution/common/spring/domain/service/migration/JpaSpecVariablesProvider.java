package com.olsonsolution.common.spring.domain.service.migration;

import com.olsonsolution.common.migration.domain.port.repository.VariablesProvider;
import com.olsonsolution.common.spring.domain.port.config.jpa.JpaSpecConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JpaSpecVariablesProvider implements VariablesProvider {

    private final List<JpaSpecConfig> jpaSpecConfigs;

    @Override
    public Map<String, String> getVariables() {
        return jpaSpecConfigs.stream()
                .map(jpaSpecConfig -> new DefaultKeyValue<>(
                        jpaSpecConfig.getJpaSpec() + "Schema",
                        jpaSpecConfig.getSchema()
                )).collect(Collectors.toUnmodifiableMap(KeyValue::getKey, KeyValue::getValue));
    }
}
