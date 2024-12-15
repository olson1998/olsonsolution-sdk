package com.olsonsolution.common.spring.configurer.domain.port.repository;

import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;

public interface BeanLocator {

    <B> B getBean(BeanDefinition beanDefinition, Class<B> beanClass);

}
