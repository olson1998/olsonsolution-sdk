package com.olsonsolution.common.data.domain.model.sql;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlUserModel implements SqlUser {

    @NonNull
    private String username;

    @NonNull
    private String password;

}
