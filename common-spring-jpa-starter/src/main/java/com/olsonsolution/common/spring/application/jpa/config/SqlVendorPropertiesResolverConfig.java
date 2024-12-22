package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorConverter;
import com.olsonsolution.common.spring.domain.port.repository.datasource.SqlVendorResolver;
import com.olsonsolution.common.spring.domain.service.datasource.SqlVendorConversionService;
import com.olsonsolution.common.spring.domain.service.datasource.SqlVendorsResolver;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SqlVendorPropertiesResolverConfig {

    @Bean
    public SqlVendorResolver defualtSqlVendorResolver() {
        return new SqlVendorsResolver();
    }

    @Bean
    @ConfigurationPropertiesBinding
    public SqlVendorConverter sqlVendorConverter(List<SqlVendorResolver> sqlVendorResolvers) {
        return new SqlVendorConversionService(sqlVendorResolvers);
    }

}
