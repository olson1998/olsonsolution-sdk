package com.olsonsolution.common.spring.domain.service.jpa;

import com.olsonsolution.common.spring.domain.port.repository.jpa.DataSourceSpecConfigurer;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManager;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingEntityManagerFactory;
import com.olsonsolution.common.spring.domain.port.repository.jpa.RoutingPlatformTransactionManager;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DataSourceSpecConfiguringService implements DataSourceSpecConfigurer {

    private final List<JpaBeans> jpaBeans;

    public DataSourceSpecConfiguringService(Map<String, RoutingEntityManagerFactory> entityManagerFactories,
                                            Map<String, RoutingEntityManager> entityManagers,
                                            Map<String, RoutingPlatformTransactionManager> platformTransactionManagers) {
        Map<String, JpaBeans.JpaBeansBuilder> jpaBeansBuilders = new HashMap<>();
        entityManagerFactories.forEach((name, b) -> {
            JpaBeans.JpaBeansBuilder beansBuilder = jpaBeansBuilders.computeIfAbsent(name, n -> JpaBeans.builder());
            beansBuilder.entityManagerFactory(b);
        });
        entityManagers.forEach((name, b) -> {
            JpaBeans.JpaBeansBuilder beansBuilder = jpaBeansBuilders.computeIfAbsent(name, n -> JpaBeans.builder());
            beansBuilder.entityManager(b);
        });
        platformTransactionManagers.forEach(((name, b) -> {
            JpaBeans.JpaBeansBuilder beansBuilder = jpaBeansBuilders.computeIfAbsent(name, n -> JpaBeans.builder());
            beansBuilder.platformTransactionManager(b);
        }));
        this.jpaBeans = jpaBeansBuilders.entrySet()
                .stream()
                .map(schemaJpaBeans -> schemaJpaBeans.getValue().build())
                .toList();
    }

    @Override
    public void configure(DataSourceSpec dataSourceSpec) {
        jpaBeans.forEach(beans -> {
            beans.entityManagerFactory.setDataSourceSpec(dataSourceSpec);
            beans.entityManager.setDataSourceSpec(dataSourceSpec);
            beans.platformTransactionManager.setDataSourceSpec(dataSourceSpec);
        });
    }

    @Override
    public void clear() {
        jpaBeans.forEach(beans -> {
            beans.entityManagerFactory.clear();
            beans.entityManager.clear();
            beans.platformTransactionManager.clear();
        });
    }

    @Builder
    private record JpaBeans(@NonNull RoutingEntityManagerFactory entityManagerFactory,
                            @NonNull RoutingEntityManager entityManager,
                            @NonNull RoutingPlatformTransactionManager platformTransactionManager) {

    }

}
