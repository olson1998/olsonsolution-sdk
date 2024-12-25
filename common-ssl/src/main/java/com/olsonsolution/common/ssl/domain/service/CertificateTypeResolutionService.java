package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.port.repository.CertificateTypeProvider;
import com.olsonsolution.common.ssl.domain.port.repository.CertificateTypeResolver;
import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateTypeResolutionService implements CertificateTypeResolver {

    private final Collection<? extends CertificateType> certificateTypes;

    public static CertificateTypeResolver forProviders(Collection<? extends CertificateTypeProvider> providers) {
        return providers.stream()
                .map(CertificateTypeProvider::getCertificateTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableList(),
                        CertificateTypeResolutionService::new
                ));
    }

    @Override
    public Optional<? extends CertificateType> resolve(String type) {
        CertificateType matchedCertType = null;
        Iterator<? extends CertificateType> keyStoreTypesIterator = certificateTypes.iterator();
        while (keyStoreTypesIterator.hasNext() && matchedCertType == null) {
            CertificateType certificateType = keyStoreTypesIterator.next();
            if (StringUtils.equals(type, certificateType.getType())) {
                matchedCertType = certificateType;
            }
        }
        return Optional.ofNullable(matchedCertType);
    }
}
