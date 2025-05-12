package com.olsonsolution.common.spring.domain.service.context;

import com.olsonsolution.common.spring.domain.model.context.exception.LocalContextNotInstanceException;
import com.olsonsolution.common.spring.domain.model.context.exception.LocalContextNotSetException;
import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.spring.domain.service.async.AbstractThreadLocalAware;
import lombok.NonNull;

import java.util.Optional;

public class LocalContextManagingService extends AbstractThreadLocalAware<LocalContext> implements LocalContextManager {

    @Override
    public void setThreadLocal(@NonNull LocalContext value) {
        Thread thread = Thread.currentThread();
        long threadId = thread.getId();
        String threadName = value.getType().getSimpleName() + "-" + threadId;
        thread.setName(threadName);
        super.setThreadLocal(value);
    }

    @Override
    public LocalContext getThreadLocal() {
        LocalContext localContext = super.getThreadLocal();
        if (localContext == null) {
            throw new LocalContextNotSetException();
        }
        return localContext;
    }

    @Override
    public <C extends LocalContext> C getThreadLocalAs(@NonNull Class<C> contextClass) {
        Optional<C> localContext = obtainThreadLocalAs(contextClass);
        if (localContext.isPresent() && contextClass.isInstance(localContext.get())) {
            return localContext.get();
        } else if (localContext.isPresent() && !contextClass.isInstance(localContext.get())) {
            throw LocalContextNotInstanceException.ofInstance(localContext.get(), contextClass);
        } else {
            throw new LocalContextNotSetException();
        }
    }

    @Override
    public <C extends LocalContext> Optional<C> obtainThreadLocalAs(@NonNull Class<C> contextClass) {
        return obtainThreadLocal()
                .filter(contextClass::isInstance)
                .map(contextClass::cast);
    }

}
