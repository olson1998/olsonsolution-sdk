package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextMetadata;
import lombok.Builder;
import lombok.Data;
import org.joda.time.MutableDateTime;

@Data
@Builder
public class ThreadLocalContextMetadata implements ContextMetadata {

    private final MutableDateTime startTimestamp;

}
