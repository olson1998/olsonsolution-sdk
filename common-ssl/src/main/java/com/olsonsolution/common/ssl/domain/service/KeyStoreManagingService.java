package com.olsonsolution.common.ssl.domain.service;

import com.olsonsolution.common.ssl.domain.model.FileKeyStore;
import com.olsonsolution.common.ssl.domain.port.repository.*;
import com.olsonsolution.common.ssl.domain.port.stereotype.CertificateType;
import com.olsonsolution.common.ssl.domain.port.stereotype.FileKeyStoreSpec;
import com.olsonsolution.common.ssl.domain.port.stereotype.KeyStoreType;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class KeyStoreManagingService implements KeyStoreManager {

    private final Map<String, FileKeyStore> keyStores;

    private final CertificateFactory certificateFactory;

    private final KeyStoreTypeResolver keyStoreTypeResolver;

    private final CertificateTypeResolver certificateTypeResolver;

    public static KeyStoreManager create(CertificateFactory certificateFactory,
                                         KeyStoreTypeResolver keyStoreTypeResolver,
                                         CertificateTypeResolver certificateTypeResolver,
                                         Collection<? extends KeyStoreSpecProvider> providers) {
        Map<String, FileKeyStore> keyStores = providers.stream()
                .map(KeyStoreSpecProvider::getKeyStoreSpecifications)
                .flatMap(Collection::stream)
                .map(KeyStoreManagingService::obtainKeyStore)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new KeyStoreManagingService(
                keyStores,
                certificateFactory,
                keyStoreTypeResolver,
                certificateTypeResolver
        );
    }

    @Override
    public Optional<Path> addCertificate(@NonNull String alias,
                                         @NonNull String certificate,
                                         @NonNull String certificateType) {
        Optional<? extends CertificateType> matchedCertType = certificateTypeResolver.resolve(certificateType);
        if (matchedCertType.isPresent()) {
            Certificate certificateObj = certificateFactory.fabricate(certificate, matchedCertType.get());
            return addCertificate(alias, certificateObj);
        } else {
            log.warn("Failed to resolver Certificate type for value: '{}'", certificate);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Path> addCertificate(@NonNull String alias, @NonNull Certificate certificate) {
        Optional<Map.Entry<String, FileKeyStore>> matchedNameKeyStore = findFileKeyStoreByCert(certificate);
        if (matchedNameKeyStore.isPresent()) {
            Map.Entry<String, FileKeyStore> namedKeyStore = matchedNameKeyStore.get();
            FileKeyStore fileKeyStore = namedKeyStore.getValue();
            addCertificate(namedKeyStore.getKey(), fileKeyStore, alias, certificate);
            return Optional.of(fileKeyStore)
                    .map(FileKeyStore::file)
                    .map(File::toPath);
        } else {
            log.warn("Failed to provision SSL Certificate, reason: Unsupported certificate type: {}",
                    certificate.getClass());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Path> addCertificate(@NonNull String keyStoreName,
                                         @NonNull String alias,
                                         @NonNull Certificate certificate) {
        Optional<FileKeyStore> fileKeyStore = findKeyStore(keyStoreName);
        fileKeyStore.ifPresentOrElse(
                keyStore -> addCertificate(keyStoreName, keyStore, alias, certificate),
                () -> log.warn("Key store: '{}' has not been found", keyStoreName)
        );
        return fileKeyStore.map(FileKeyStore::file).map(File::toPath);
    }

    @Override
    public Optional<Path> addCertificate(@NonNull String keyStoreName,
                                         @NonNull String alias,
                                         @NonNull String certificate,
                                         @NonNull String certificateType) {
        Optional<? extends CertificateType> matchedCertType = certificateTypeResolver.resolve(certificateType);
        if (matchedCertType.isPresent()) {
            Certificate certificateObj = certificateFactory.fabricate(certificate, matchedCertType.get());
            return addCertificate(keyStoreName, alias, certificateObj);
        } else {
            log.warn("Failed to resolver Certificate type for value: '{}'", certificate);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Path> findKeyStorePathByAlias(@NonNull String alias) {
        Optional<FileKeyStore> fileKeyStore = Optional.empty();
        Iterator<FileKeyStore> fileKeyStoreIterator = keyStores.values().iterator();
        while (fileKeyStoreIterator.hasNext() && fileKeyStore.isEmpty()) {
            FileKeyStore currentFileKeyStore = fileKeyStoreIterator.next();
            if (doesContainsCertificateAlias(currentFileKeyStore, alias)) {
                fileKeyStore = Optional.of(currentFileKeyStore);
            }
        }
        return fileKeyStore.map(FileKeyStore::file).map(File::toPath);
    }

    private static Optional<Map.Entry<String, FileKeyStore>> obtainKeyStore(FileKeyStoreSpec spec) {
        String name = spec.getName();
        try {
            File keyStoreFile = spec.getLocation();
            KeyStore keyStore = KeyStore.getInstance(keyStoreFile, spec.getPassword().toCharArray());
            return Optional.of(entry(name, new FileKeyStore(keyStoreFile, keyStore)));
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            log.error("Failed to obtain key store: '{}', reason", name, e);
            return Optional.empty();
        }
    }

    private synchronized void addCertificate(String keyStoreName,
                                             FileKeyStore fileKeyStore,
                                             String alias,
                                             Certificate certificate) {
        File file = fileKeyStore.file();
        KeyStore keyStore = fileKeyStore.keyStore();
        try {
            if (keyStore.isCertificateEntry(alias)) {
                log.warn(
                        "Key store: '{}' in path: '{}' already contains entry with alias: '{}'",
                        keyStoreName,
                        file,
                        alias
                );
            } else {
                keyStore.setCertificateEntry(alias, certificate);
                log.info("Key store: '{}' provisioned SSL certificate with alias: '{}'", keyStore, alias);
            }
        } catch (KeyStoreException e) {
            log.warn(
                    "Key store: '{}' failed to provision SSL Certificate with alias: '{}', reason:",
                    keyStoreName, alias, e
            );
        }
    }

    private Optional<FileKeyStore> findKeyStore(String keyStoreName) {
        return Optional.ofNullable(keyStores.get(keyStoreName));
    }

    private Optional<Map.Entry<String, FileKeyStore>> findFileKeyStoreByCert(Certificate certificate) {
        Optional<Map.Entry<String, FileKeyStore>> namedFileKeyStore = Optional.empty();
        Iterator<Map.Entry<String, FileKeyStore>> fileKeyStoreIterator = keyStores.entrySet().iterator();
        while (fileKeyStoreIterator.hasNext() && namedFileKeyStore.isEmpty()) {
            Map.Entry<String, FileKeyStore> nextNamedFileKeyStore = fileKeyStoreIterator.next();
            FileKeyStore fileKeyStore = nextNamedFileKeyStore.getValue();
            if (isKeyStoreSupportingCert(fileKeyStore, certificate)) {
                namedFileKeyStore = Optional.of(nextNamedFileKeyStore);
            }
        }
        return namedFileKeyStore;
    }

    private boolean isKeyStoreSupportingCert(FileKeyStore fileKeyStore, Certificate certificate) {
        return findKeyStoreType(fileKeyStore.keyStore())
                .flatMap(type -> Optional.of(fileKeyStore)
                        .map(file -> isSupportedCertificate(type, certificate)))
                .orElse(false);
    }

    private Optional<KeyStoreType> findKeyStoreType(KeyStore keyStore) {
        return Optional.ofNullable(keyStore)
                .map(KeyStore::getType)
                .flatMap(keyStoreTypeResolver::resolve);
    }

    private boolean isSupportedCertificate(KeyStoreType keyStoreType, Certificate certificate) {
        return keyStoreType.getSupportedCertificates()
                .stream()
                .anyMatch(certType -> certType.isInstance(certificate));
    }

    private boolean doesContainsCertificateAlias(FileKeyStore fileKeyStore, String alias) {
        KeyStore keyStore = fileKeyStore.keyStore();
        try {
            return keyStore.isCertificateEntry(alias);
        } catch (KeyStoreException e) {
            log.debug("Failed to lookup key store, reason", e);
            return false;
        }
    }

}
