package com.olsonsolution.common.spring.application.config.jpa;

import com.olsonsolution.common.spring.application.datasource.common.entity.AuditableEntity;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.joda.time.MutableDateTime;

public class TimeAuditingEntityListener {

    static TimeUtils TIME_UTILS;

    @PrePersist
    public void setCreationTimestamp(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            MutableDateTime timestamp = getTimestamp();
            auditable.setCreationTimestamp(timestamp);
            auditable.setLastUpdateTimestamp(timestamp);
            auditable.setVersion(1L);
        }
    }

    @PreUpdate
    public void setLastUpdateTimestamp(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            auditable.setCreationTimestamp(auditable.getCreationTimestamp());
            auditable.setLastUpdateTimestamp(getTimestamp());
        }
    }

    private MutableDateTime getTimestamp() {
        if (TIME_UTILS != null) {
            return TIME_UTILS.getTimestamp();
        } else {
            return MutableDateTime.now();
        }
    }

}
