package com.olsonsolution.common.databind.domain.port.sterotype;

public interface TypeBind<A, T extends A> {

    Class<A> getAbstractClass();

    Class<T> getJavaClass();

}
