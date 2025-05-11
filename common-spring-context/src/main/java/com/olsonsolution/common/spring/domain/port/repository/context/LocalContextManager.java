package com.olsonsolution.common.spring.domain.port.repository.context;

import com.olsonsolution.common.spring.domain.port.async.repository.ThreadLocalAware;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import lombok.NonNull;

import java.util.Optional;

public interface LocalContextManager extends ThreadLocalAware<LocalContext> {

    <C extends LocalContext> C getThreadLocalAs(@NonNull Class<C> contextClass);

    <C extends LocalContext> Optional<C> obtainThreadLocalAs(@NonNull Class<C> contextClass);

}
