package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;

import java.util.Optional;

public interface CertificateTypeResolver {

    Optional<? extends CertificateType> resolve(String type);

}
