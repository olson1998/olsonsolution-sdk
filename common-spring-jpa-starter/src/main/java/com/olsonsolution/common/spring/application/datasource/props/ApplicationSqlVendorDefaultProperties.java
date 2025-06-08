package com.olsonsolution.common.spring.application.datasource.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.spring.domain.port.props.datasource.SqlVendorDefaultsProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSqlVendorDefaultProperties implements SqlVendorDefaultsProperties {

    private SqlVendor vendorName;

    private String catalog;

    private String schema;

}
