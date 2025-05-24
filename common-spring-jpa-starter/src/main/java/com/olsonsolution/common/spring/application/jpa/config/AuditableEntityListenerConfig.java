package com.olsonsolution.common.spring.application.jpa.config;

import com.olsonsolution.common.spring.application.jpa.service.AuditableEntityListener;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuditableEntityListenerConfig implements InitializingBean {

    private final ObjectProvider<TimeUtils> timeUtilsObjectProvider;

    @Override
    public void afterPropertiesSet() throws Exception {
        timeUtilsObjectProvider.ifAvailable(timeUtils -> AuditableEntityListener.timeUtils = timeUtils);
    }
}
