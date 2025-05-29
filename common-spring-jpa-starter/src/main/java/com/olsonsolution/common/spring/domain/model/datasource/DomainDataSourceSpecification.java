package com.olsonsolution.common.spring.domain.model.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpecification;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainDataSourceSpecification implements DataSourceSpecification {

    @NonNull
    private String name;

    @NonNull
    private SqlPermission permission;

}
