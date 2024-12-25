package com.olsonsolution.common.ssl.domain.port.stereotype;

import lombok.NonNull;

import java.security.Provider;

public interface CertificateType {

    @NonNull
    String getType();

    Provider getProvider();

}
