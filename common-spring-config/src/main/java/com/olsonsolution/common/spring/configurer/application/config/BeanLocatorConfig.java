package com.olsonsolution.common.spring.configurer.application.config;

import com.olsonsolution.common.spring.configurer.domain.port.repository.BeanLocator;
import com.olsonsolution.common.spring.configurer.domain.service.BeanLookupService;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanLocatorConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public BeanLocator beanLocator() {
        return new BeanLookupService(applicationContext);
    }

}
