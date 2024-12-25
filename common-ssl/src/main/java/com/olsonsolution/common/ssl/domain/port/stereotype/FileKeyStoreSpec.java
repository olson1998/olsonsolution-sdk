package com.olsonsolution.common.ssl.domain.port.stereotype;

import lombok.NonNull;

import java.io.File;

public interface FileKeyStoreSpec {

    @NonNull
    String getName();

    @NonNull
    File getLocation();

    @NonNull
    String getPassword();

}
