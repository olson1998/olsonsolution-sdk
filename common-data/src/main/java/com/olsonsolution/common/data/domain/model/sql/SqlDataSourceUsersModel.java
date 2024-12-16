package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SqlDataSourceUsersModel<U extends SqlUser> implements SqlDataSourceUsers {

    private Collection<U> readOnly;

    private Collection<U> writeOnly;

    private Collection<U> readWrite;

    private Collection<U> readWriteExecute;

}
