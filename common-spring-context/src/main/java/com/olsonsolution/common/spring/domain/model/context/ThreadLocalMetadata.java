package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ThreadMetadata;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadLocalMetadata implements ThreadMetadata {

    private final long threadId;

    private final String threadName;

}
