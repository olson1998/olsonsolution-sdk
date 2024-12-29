package com.olsonsolution.common.spring.application.jpa.props;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSqlUserProperties implements SqlUser {

    private String username;

    private String password;

}
