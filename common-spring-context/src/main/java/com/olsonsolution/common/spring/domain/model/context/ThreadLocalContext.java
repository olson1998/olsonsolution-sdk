package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextMetadata;
import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextType;
import com.olsonsolution.common.spring.domain.port.stereotype.context.LocalContext;
import lombok.*;

@Data
@Builder
public class ThreadLocalContext implements LocalContext {

    private final String id;

    private final ContextType type;

    private final ContextMetadata metadata;

}
