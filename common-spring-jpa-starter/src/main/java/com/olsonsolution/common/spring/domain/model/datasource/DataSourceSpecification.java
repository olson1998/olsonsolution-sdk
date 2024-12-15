package com.olsonsolution.common.spring.domain.model.datasource;

import com.olsonsolution.common.data.domain.model.sql.SqlPermissions;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlPermission;
import com.olsonsolution.common.spring.domain.port.stereotype.datasource.DataSourceSpec;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSpecification implements DataSourceSpec {

    @NonNull
    private String name;

    @NonNull
    private SqlPermissions permissions;

    @Override
    public SqlPermission getPermission() {
        return permissions;
    }
}
