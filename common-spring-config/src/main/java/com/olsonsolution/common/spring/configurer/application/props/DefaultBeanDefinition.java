package com.olsonsolution.common.spring.configurer.application.props;

import com.olsonsolution.common.spring.configurer.domain.port.stereotype.BeanDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultBeanDefinition implements BeanDefinition {

    private Class<?> javaClass;

    private String beanName;

}
