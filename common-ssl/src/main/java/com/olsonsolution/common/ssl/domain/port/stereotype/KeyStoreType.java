package com.olsonsolution.common.ssl.domain.port.stereotype;

import java.security.cert.Certificate;
import java.util.Set;

public interface KeyStoreType {

    String getType();

    Set<Class<? extends Certificate>> getSupportedCertificates();

}
