package com.olsonsolution.common.ssl.domain.model;

import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.security.Provider;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JdkCertificates implements CertificateType {

    X509("X.509", null);

    @NonNull
    private final String type;

    private final Provider provider;

}
