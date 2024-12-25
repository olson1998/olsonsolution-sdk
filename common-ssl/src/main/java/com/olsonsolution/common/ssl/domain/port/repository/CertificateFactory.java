package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.NonNull;

import java.security.cert.Certificate;

public interface CertificateFactory {

    Certificate fabricate(@NonNull String certificate,@NonNull CertificateType certificateType);

}
