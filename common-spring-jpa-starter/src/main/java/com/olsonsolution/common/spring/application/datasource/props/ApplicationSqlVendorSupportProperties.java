package com.olsonsolution.common.spring.application.datasource.props;

import com.olsonsolution.common.spring.domain.port.props.datasource.SqlVendorDefaultsProperties;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlVendorSupportProperties;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.olsonsolution.common.spring.application.datasource.props.ApplicationSqlVendorSupportProperties.SPRING_APPLICATION_DATA_SOURCE_SQL_VENDOR_PROPERTIES_PREFIX;

@Data
@Configuration
@ConditionalOnBean(ApplicationDestinationDataSourceProperties.class)
@ConfigurationProperties(prefix = SPRING_APPLICATION_DATA_SOURCE_SQL_VENDOR_PROPERTIES_PREFIX)
public class ApplicationSqlVendorSupportProperties implements SqlVendorSupportProperties {

    public static final String SPRING_APPLICATION_DATA_SOURCE_SQL_VENDOR_PROPERTIES_PREFIX =
            "spring.application.data-source.sql-vendor";

    private final List<ApplicationSqlVendorDefaultProperties> defaults = new ArrayList<>();

    @Override
    public Collection<? extends SqlVendorDefaultsProperties> getVendorDefaults() {
        return defaults;
    }
}
