package com.olsonsolution.common.migration.domain.port.repository;

import com.olsonsolution.common.migration.domain.port.stereotype.ChangeLog;

import java.util.Collection;

public interface ChangelogProvider {

    Collection<? extends ChangeLog> getChangelogs();

}
