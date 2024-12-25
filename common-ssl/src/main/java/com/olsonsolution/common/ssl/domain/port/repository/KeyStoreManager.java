package com.olsonsolution.common.ssl.domain.port.repository;

import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import lombok.NonNull;

import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.Optional;

public interface KeyStoreManager {

    Optional<Path> addCertificate(@NonNull String alias,
                                  @NonNull String certificate,
                                  @NonNull String certificateType);

    Optional<Path> addCertificate(@NonNull String alias, @NonNull Certificate certificate);

    Optional<Path> addCertificate(@NonNull String keyStoreName,
                                  @NonNull String alias,
                                  @NonNull Certificate certificate);

    Optional<Path> addCertificate(@NonNull String keyStoreName,
                                  @NonNull String alias,
                                  @NonNull String certificate,
                                  @NonNull String certificateType);

    Optional<Path> findKeyStorePathByAlias(@NonNull String alias);

}
