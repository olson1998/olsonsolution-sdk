package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;

import java.util.Collection;

public interface KeyStoreTypeProvider {

    Collection<? extends KeyStoreType> getKeyStoreTypes();

}
