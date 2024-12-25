package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;

import java.util.Optional;

public interface KeyStoreTypeResolver {

    Optional<? extends KeyStoreType> resolve(String type);

}
