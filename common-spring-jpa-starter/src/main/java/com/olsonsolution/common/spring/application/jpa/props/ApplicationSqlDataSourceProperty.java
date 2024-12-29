package com.olsonsolution.common.spring.application.jpa.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSqlDataSourceProperty {

    private String name;

    private String value;

}
