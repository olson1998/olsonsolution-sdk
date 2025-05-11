package com.olsonsolution.common.spring.domain.port.stereotype;

import lombok.NonNull;

public interface LocalContext {

    @NonNull
    String getId();

    @NonNull
    ContextType getType();

    @NonNull
    ContextMetadata getMetadata();

    @NonNull
    ThreadMetadata getThreadMetadata();

}
