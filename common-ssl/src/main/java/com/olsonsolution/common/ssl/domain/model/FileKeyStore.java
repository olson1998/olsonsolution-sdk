package com.olsonsolution.common.ssl.domain.model;

import lombok.NonNull;

import java.io.File;
import java.security.KeyStore;

public record FileKeyStore(@NonNull File file, @NonNull KeyStore keyStore) {
}
