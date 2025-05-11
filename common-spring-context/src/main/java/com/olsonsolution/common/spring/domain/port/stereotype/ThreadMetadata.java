package com.olsonsolution.common.spring.domain.port.stereotype;

import lombok.NonNull;

public interface ThreadMetadata {

    long getThreadId();

    @NonNull
    String getThreadName();

}
