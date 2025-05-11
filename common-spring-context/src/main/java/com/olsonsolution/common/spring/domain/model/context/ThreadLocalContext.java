package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextMetadata;
import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextType;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import com.olsonsolution.common.spring.domain.port.stereotype.context.ThreadMetadata;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadLocalContext implements LocalContext {

    private final String id;

    private final ContextType type;

    private final ContextMetadata metadata;

    private final ThreadMetadata threadMetadata;

}
