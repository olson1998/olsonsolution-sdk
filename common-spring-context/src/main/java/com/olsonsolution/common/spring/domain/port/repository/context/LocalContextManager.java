package com.olsonsolution.common.spring.domain.port.repository.context;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;

import java.util.Optional;

public interface LocalContextManager extends ThreadLocalAware<LocalContext> {

    <C extends LocalContext> Optional<C> obtainThreadLocalAs(Class<C> contextClass);

}
