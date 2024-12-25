package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.port.repository.CertificateFactory;
import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CertificateFabricatingService implements CertificateFactory {

    @Override
    public Certificate fabricate(@NonNull String certificate, @NonNull CertificateType certificateType) {
        String type = certificateType.getType();
        Provider provider = certificateType.getProvider();
        java.security.cert.CertificateFactory certificateFactory = getCertificateFactory(type, provider);
        try (InputStream certInput = new ByteArrayInputStream(certificate.getBytes(UTF_8))) {
            return certificateFactory.generateCertificate(certInput);
        } catch (CertificateException | IOException e) {

        }
        return null;
    }

    private java.security.cert.CertificateFactory getCertificateFactory(String type, Provider provider) {
        try {
            if (provider != null) {
                return java.security.cert.CertificateFactory.getInstance(type, provider);
            } else {
                return java.security.cert.CertificateFactory.getInstance(type);
            }
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

}
