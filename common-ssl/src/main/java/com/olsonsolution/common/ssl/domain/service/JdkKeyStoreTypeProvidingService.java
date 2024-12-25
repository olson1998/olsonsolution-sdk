package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.model.JdkKeyStores;
import com.olsonsolution.common.ssl.domain.port.repository.KeyStoreTypeProvider;
import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;

@Data
public class JdkKeyStoreTypeProvidingService implements KeyStoreTypeProvider {

    private final Collection<? extends KeyStoreType> keyStoreTypes =
            Arrays.asList(JdkKeyStores.values());

}
