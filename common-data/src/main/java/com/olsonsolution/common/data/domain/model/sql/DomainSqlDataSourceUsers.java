package com.olsonsolution.common.data.domain.model.sql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainSqlDataSourceUsers implements SqlDataSourceUsers {

    @JsonDeserialize(contentAs = DomainSqlUser.class)
    private SqlUser readOnly;

    @JsonDeserialize(contentAs = DomainSqlUser.class)
    private SqlUser writeOnly;

    @JsonDeserialize(contentAs = DomainSqlUser.class)
    private SqlUser readWrite;

    @JsonDeserialize(contentAs = DomainSqlUser.class)
    private SqlUser readWriteExecute;

}
