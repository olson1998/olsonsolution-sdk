package com.olsonsolution.common.jackson.domain.port.sterotype;

public interface TypeBind<A, T extends A> {

    Class<A> getAbstractClass();

    Class<T> getJavaClass();

}
