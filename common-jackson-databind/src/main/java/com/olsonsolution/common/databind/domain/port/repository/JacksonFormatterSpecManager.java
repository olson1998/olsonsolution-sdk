package com.olsonsolution.common.databind.domain.port.repository;

import com.olsonsolution.common.databind.domain.port.stereotype.JacksonFormatterSpec;

import java.util.Optional;

public interface JacksonFormatterSpecManager {

    Optional<? extends JacksonFormatterSpec> findThreadLocal();

    void setThreadLocal(JacksonFormatterSpec jacksonFormatterSpec);

    void clear();

}
