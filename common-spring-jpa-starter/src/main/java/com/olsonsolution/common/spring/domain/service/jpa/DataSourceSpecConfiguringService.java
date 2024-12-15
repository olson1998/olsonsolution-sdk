package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceSpecConfiguringService implements DataSourceSpecConfigurer {

    private final List<JpaBeans> jpaBeans = new ArrayList<>();

    @Override
    public void register(Map<String, RoutingEntityManagerFactory> routingEntityManagerFactories,
                         Map<String, RoutingPlatformTransactionManager> routingPlatformTransactionManagers) {
        Map<String, JpaBeans.JpaBeansBuilder> jpaBeansBuilders = new HashMap<>();
        routingEntityManagerFactories.forEach((name, b) -> {
            JpaBeans.JpaBeansBuilder beansBuilder =
                    jpaBeansBuilders.computeIfAbsent(name, n -> JpaBeans.builder());
            beansBuilder.entityManagerFactory(b);
        });
        routingPlatformTransactionManagers.forEach(((name, b) -> {
            JpaBeans.JpaBeansBuilder beansBuilder =
                    jpaBeansBuilders.computeIfAbsent(name, n -> JpaBeans.builder());
            beansBuilder.platformTransactionManager(b);
        }));
        jpaBeans.addAll(jpaBeansBuilders.values()
                .stream()
                .map(JpaBeans.JpaBeansBuilder::build)
                .toList());
    }

    @Override
    public void configure(DataSourceSpec dataSourceSpec) {
        jpaBeans.forEach(beans -> {
            beans.entityManagerFactory.setDataSourceSpec(dataSourceSpec);
            beans.platformTransactionManager.setDataSourceSpec(dataSourceSpec);
        });
    }

    @Override
    public void clear() {
        jpaBeans.forEach(beans -> {
            beans.entityManagerFactory.clear();
            beans.platformTransactionManager.clear();
        });
    }

    @Builder
    private record JpaBeans(@NonNull RoutingEntityManagerFactory entityManagerFactory,
                            @NonNull RoutingPlatformTransactionManager platformTransactionManager) {

    }

}
