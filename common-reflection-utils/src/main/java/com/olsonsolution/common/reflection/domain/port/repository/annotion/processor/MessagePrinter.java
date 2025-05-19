package com.olsonsolution.common.reflection.domain.port.repository.annotion.processor;

import javax.tools.Diagnostic;

public interface MessagePrinter {

    void print(Diagnostic.Kind kind, Class<?> serviceClass, String message);

    void print(Diagnostic.Kind kind, Class<?> serviceClass, String message, Throwable throwable);

}
