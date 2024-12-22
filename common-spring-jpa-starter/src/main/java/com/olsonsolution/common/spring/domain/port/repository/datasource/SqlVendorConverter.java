package com.olsonsolution.common.spring.domain.port.repository.datasource;

import com.olsonsolution.common.data.domain.port.stereotype.sql.SqlVendor;
import org.springframework.core.convert.converter.Converter;

public interface SqlVendorConverter extends Converter<String, SqlVendor> {
}
