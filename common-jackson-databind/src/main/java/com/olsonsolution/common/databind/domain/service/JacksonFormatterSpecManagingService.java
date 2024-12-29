package com.olsonsolution.common.databind.domain.service;

import com.olsonsolution.common.databind.domain.port.repository.JacksonFormatterSpecManager;
import com.olsonsolution.common.databind.domain.port.stereotype.JacksonFormatterSpec;

import java.util.Optional;

public class JacksonFormatterSpecManagingService implements JacksonFormatterSpecManager {

    private final ThreadLocal<JacksonFormatterSpec> jacksonFormatterSpecThreadLocal = new ThreadLocal<>();

    @Override
    public Optional<? extends JacksonFormatterSpec> findThreadLocal() {
        return Optional.ofNullable(jacksonFormatterSpecThreadLocal.get());
    }

    @Override
    public void setThreadLocal(JacksonFormatterSpec jacksonFormatterSpec) {
        jacksonFormatterSpecThreadLocal.set(jacksonFormatterSpec);
    }

    @Override
    public void clear() {
        jacksonFormatterSpecThreadLocal.remove();
    }
}
