package com.olsonsolution.common.spring.domain.port.stereotype.context;

import lombok.NonNull;

public interface LocalContext {

    @NonNull
    String getId();

    @NonNull
    ContextType getType();

    @NonNull
    ContextMetadata getMetadata();

}
