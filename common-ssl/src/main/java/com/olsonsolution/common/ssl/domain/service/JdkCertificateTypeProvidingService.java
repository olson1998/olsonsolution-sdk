package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.model.JdkCertificates;
import com.olsonsolution.common.ssl.domain.port.repository.CertificateTypeProvider;
import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;

@Data
public class JdkCertificateTypeProvidingService implements CertificateTypeProvider {

    private final Collection<? extends CertificateType> certificateTypes = Arrays.asList(JdkCertificates.values());

}
