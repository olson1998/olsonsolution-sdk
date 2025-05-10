package com.olsonsolution.common.spring.domain.port.async.repository;

import com.olsonsolution.common.spring.domain.port.async.props.ThreadPoolProperties;

import java.util.concurrent.Executor;

public interface ExecutorFactory {

    Executor fabricate(ThreadPoolProperties properties, ThreadLocalInheritableThreadFactory threadFactory);

}
