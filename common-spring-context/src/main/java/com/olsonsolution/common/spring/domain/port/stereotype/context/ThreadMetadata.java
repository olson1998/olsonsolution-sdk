package com.olsonsolution.common.spring.domain.port.stereotype.context;

import lombok.NonNull;

public interface ThreadMetadata {

    long getThreadId();

    @NonNull
    String getThreadName();

}
