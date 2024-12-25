package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;

import java.util.Collection;

public interface CertificateTypeProvider {

    Collection<? extends CertificateType> getCertificateTypes();

}
