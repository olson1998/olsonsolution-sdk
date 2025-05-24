package com.olsonsolution.common.spring.application.config.jpa;

import com.olsonsolution.common.time.domain.port.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@RequiredArgsConstructor
public class TimeAuditingEntityListenerConfig implements InitializingBean {

    private final ObjectProvider<TimeUtils> timeUtils;

    @Override
    public void afterPropertiesSet() throws Exception {
        timeUtils.ifAvailable(util -> TimeAuditingEntityListener.TIME_UTILS = util);
    }
}
