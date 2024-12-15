package com.olsonsolution.common.data.domain.model.sql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSource;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlDataSourceUsers;
import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import com.olsonsolution.common.data.domain.service.json.SqlDataSourcePropertiesStdDeserializer;
import com.olsonsolution.common.data.domain.service.json.SqlDataSourcePropertiesStdSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Properties;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlDataSourceModel<V extends SqlVendor, U extends SqlDataSourceUsers> implements SqlDataSource {

    private V vendor;

    private String host;

    private Integer port;

    private String database;

    @EqualsAndHashCode.Exclude
    @JsonSerialize(using = SqlDataSourcePropertiesStdSerializer.class)
    @JsonDeserialize(using = SqlDataSourcePropertiesStdDeserializer.class)
    private Properties properties;

    @EqualsAndHashCode.Exclude
    private U users;

}
