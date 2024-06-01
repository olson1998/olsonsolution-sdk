package com.olsonsolution.common.jackson.model;

import com.olsonsolution.common.jackson.domain.port.sterotype.TypeBind;
import lombok.Data;
import lombok.NonNull;

@Data
public class SimpleTypeBind<A, T extends A> implements TypeBind<A, T> {

    @NonNull
    private final Class<A> abstractClass;

    @NonNull
    private final Class<T> javaClass;

}
