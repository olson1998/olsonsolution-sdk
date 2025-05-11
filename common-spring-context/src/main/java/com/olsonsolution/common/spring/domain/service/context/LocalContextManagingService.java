package com.olsonsolution.common.spring.domain.service.context;

import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;

import java.util.Optional;

public class LocalContextManagingService extends AbstractThreadLocalAware<LocalContext> implements LocalContextManager {

    @Override
    public <C extends LocalContext> Optional<C> obtainThreadLocalAs(Class<C> contextClass) {
        return obtainThreadLocal()
                .filter(contextClass::isInstance)
                .map(contextClass::cast);
    }

}
