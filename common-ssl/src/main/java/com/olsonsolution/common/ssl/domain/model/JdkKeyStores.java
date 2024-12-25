package com.olsonsolution.common.ssl.domain.model;

import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JdkKeyStores implements KeyStoreType {

    JKS("JKS", Collections.singleton(X509Certificate.class)),
    PCKS12("PKCS12", Collections.singleton(X509Certificate.class)),
    JCEKS("JCEKS", Collections.singleton(X509Certificate.class));

    private final String type;

    private final Set<Class<? extends Certificate>> supportedCertificates;

}
