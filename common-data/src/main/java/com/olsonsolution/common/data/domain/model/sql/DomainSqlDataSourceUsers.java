package com.olsonsolution.common.data.domain.model.sql;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @Singular("ro")
    private List<? extends SqlUser> readOnly;

    @Singular("wo")
    private List<? extends SqlUser> writeOnly;

    @Singular("rw")
    private List<? extends SqlUser> readWrite;

    @Singular("rwx")
    private List<? extends SqlUser> readWriteExecute;

}
