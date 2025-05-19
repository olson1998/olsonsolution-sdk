package com.olsonsolution.common.reflection.domain.service.annotion.processor;

import com.olsonsolution.common.reflection.domain.port.repository.annotion.processor.MessagePrinter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.time.Instant;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@RequiredArgsConstructor
public class MessagePrintingService implements MessagePrinter {

    private static final String MESSAGE_FORMAT = "%s %s %s: %s";

    private final Messager messager;

    @Override
    public void print(Diagnostic.Kind kind, Class<?> serviceClass, String message) {
        Instant timestamp = Instant.now();
        String msg = MESSAGE_FORMAT
                .formatted(serviceClass.getSimpleName(), kind.name(), ISO_INSTANT.format(timestamp), message);
        messager.printMessage(kind, msg);
    }

    @Override
    public void print(Diagnostic.Kind kind, Class<?> serviceClass, String message, Throwable throwable) {
        String msg = message + "\n" + ExceptionUtils.getMessage(throwable) +
                "\n" + ExceptionUtils.getStackTrace(throwable);
        print(kind, serviceClass, msg);
    }
}
