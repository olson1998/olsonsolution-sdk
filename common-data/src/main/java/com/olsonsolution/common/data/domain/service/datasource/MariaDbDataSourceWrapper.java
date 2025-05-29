package com.olsonsolution.common.data.domain.service.datasource;

import lombok.Getter;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;

@Getter
public class MariaDbDataSourceWrapper extends MariaDbDataSource {

    private String password;

    @Override
    public void setPassword(String password) throws SQLException {
        super.setPassword(password);
        this.password = password;
    }
}
