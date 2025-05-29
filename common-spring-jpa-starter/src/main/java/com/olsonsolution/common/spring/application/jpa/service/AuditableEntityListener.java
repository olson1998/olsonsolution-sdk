package com.olsonsolution.common.spring.application.jpa.service;

import com.olsonsolution.common.spring.application.datasource.model.audit.AuditableEntity;
import com.olsonsolution.common.spring.application.hibernate.EmbeddedTimestamp;
import com.olsonsolution.common.time.domain.port.TimeUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.joda.time.MutableDateTime;

public class AuditableEntityListener {

    public static TimeUtils timeUtils;

    @PrePersist
    public void onSave(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            MutableDateTime timestamp = obtainTimestamp();
            EmbeddedTimestamp embeddedTimestamp = new EmbeddedTimestamp(timestamp);
            auditable.setCreation(embeddedTimestamp);
            auditable.setLastUpdate(embeddedTimestamp);
            auditable.setVersion(1L);
        }
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            MutableDateTime timestamp = obtainTimestamp();
            EmbeddedTimestamp embeddedTimestamp = new EmbeddedTimestamp(timestamp);
            auditable.setCreation(auditable.getCreation());
            auditable.setLastUpdate(embeddedTimestamp);
        }
    }

    private MutableDateTime obtainTimestamp() {
        if (timeUtils == null) {
            return MutableDateTime.now();
        } else {
            return timeUtils.getTimestamp();
        }
    }

}
