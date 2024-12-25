package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.port.repository.KeyStoreTypeProvider;
import com.olsonsolution.common.ssl.domain.port.repository.KeyStoreTypeResolver;
import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyStoreTypeResolutionService implements KeyStoreTypeResolver {

    private final Collection<? extends KeyStoreType> keyStoreTypes;

    public static KeyStoreTypeResolver forProviders(Collection<? extends KeyStoreTypeProvider> providers) {
        return providers.stream()
                .map(KeyStoreTypeProvider::getKeyStoreTypes)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableList(),
                        KeyStoreTypeResolutionService::new
                ));
    }

    @Override
    public Optional<? extends KeyStoreType> resolve(String type) {
        KeyStoreType matchedKeyStoreType = null;
        Iterator<? extends KeyStoreType> keyStoreTypesIterator = keyStoreTypes.iterator();
        while (keyStoreTypesIterator.hasNext() && matchedKeyStoreType == null) {
            KeyStoreType keyStoreType  = keyStoreTypesIterator.next();
            if(StringUtils.equals(type, keyStoreType.getType())) {
                matchedKeyStoreType = keyStoreType;
            }
        }
        return Optional.ofNullable(matchedKeyStoreType);
    }
}
