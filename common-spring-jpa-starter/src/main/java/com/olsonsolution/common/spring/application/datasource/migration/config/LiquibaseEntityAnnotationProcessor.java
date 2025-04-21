package com.olsonsolution.common.spring.application.datasource.migration.config;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)

public class LiquibaseEntityAnnotationProcessor {
}
