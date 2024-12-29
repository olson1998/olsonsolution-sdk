package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicationSqlDataSourceUsersProperties implements SqlDataSourceUsers {

    private final List<ApplicationSqlUserProperties> ro = new ArrayList<>();

    private final List<ApplicationSqlUserProperties> wo = new ArrayList<>();

    private final List<ApplicationSqlUserProperties> rw = new ArrayList<>();

    private final List<ApplicationSqlUserProperties> rwx = new ArrayList<>();

    @Override
    public List<? extends SqlUser> getReadOnly() {
        return ro;
    }

    @Override
    public List<? extends SqlUser> getWriteOnly() {
        return wo;
    }

    @Override
    public List<? extends SqlUser> getReadWrite() {
        return rw;
    }

    @Override
    public List<? extends SqlUser> getReadWriteExecute() {
        return rwx;
    }

}
