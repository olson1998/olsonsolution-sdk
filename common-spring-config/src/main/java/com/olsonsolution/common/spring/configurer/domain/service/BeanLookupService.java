package com.olsonsolution.common.spring.configurer.domain.service;

import com.olsonsolution.common.spring.configurer.domain.port.repository.BeanLocator;
import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
public class BeanLookupService implements BeanLocator {

    private final ApplicationContext applicationContext;

    @Override
    public <B> B getBean(BeanDefinition beanDefinition, Class<B> beanClass) {
        String beanName = beanDefinition.getBeanName();
        Class<?> javaClass = beanDefinition.getJavaClass();
        if(beanName != null && javaClass != null) {
            return beanClass.cast(applicationContext.getBean(beanName, javaClass));
        } else if (javaClass != null) {
            return beanClass.cast(applicationContext.getBean(javaClass));
        } else {
            throw new IllegalStateException("Bean definition not found");
        }
    }
}
