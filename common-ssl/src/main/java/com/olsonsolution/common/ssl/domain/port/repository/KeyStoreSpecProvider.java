package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.FileKeyStoreSpec;

import java.util.Collection;

public interface KeyStoreSpecProvider {

    Collection<? extends FileKeyStoreSpec> getKeyStoreSpecifications();

}
