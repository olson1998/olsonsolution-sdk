package com.olsonsolution.common.spring.application;

import com.olsonsolution.common.ssl.domain.port.repository.*;
import com.olsonsolution.common.ssl.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SSLConfig {

    @Bean
    public CertificateFactory certificateFactory() {
        return new CertificateFabricatingService();
    }

    @Bean
    public KeyStoreTypeProvider jdkKeyStoreTypeProvider() {
        return new JdkKeyStoreTypeProvidingService();
    }

    @Bean
    public CertificateTypeProvider jdkCertificateTypeProvider() {
        return new JdkCertificateTypeProvidingService();
    }

    @Bean
    public KeyStoreTypeResolver keyStoreTypeResolver(List<KeyStoreTypeProvider> providers) {
        return KeyStoreTypeResolutionService.forProviders(providers);
    }

    @Bean
    public CertificateTypeResolver certificateTypeResolver(List<CertificateTypeProvider> providers) {
        return CertificateTypeResolutionService.forProviders(providers);
    }

    @Bean
    public KeyStoreManager keyStoreManager(CertificateFactory certificateFactory,
                                           KeyStoreTypeResolver keyStoreTypeResolver,
                                           CertificateTypeResolver certificateTypeResolver,
                                           List<KeyStoreSpecProvider> keyStoreSpecProviders) {
        return KeyStoreManagingService.create(
                certificateFactory,
                keyStoreTypeResolver,
                certificateTypeResolver,
                keyStoreSpecProviders
        );
    }

}
