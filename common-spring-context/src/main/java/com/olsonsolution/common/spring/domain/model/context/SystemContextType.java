package com.olsonsolution.common.spring.domain.model.context;

import com.olsonsolution.common.spring.domain.port.stereotype.context.ContextType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SystemContextType implements ContextType {

    TEST("test"),
    SYSTEM("system");

    private final String simpleName;

}
