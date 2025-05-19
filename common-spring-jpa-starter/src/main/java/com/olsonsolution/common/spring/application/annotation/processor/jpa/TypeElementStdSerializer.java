package com.olsonsolution.common.spring.application.annotation.processor.jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.lang.model.element.TypeElement;
import java.io.IOException;

public class TypeElementStdSerializer extends StdSerializer<TypeElement> {

    TypeElementStdSerializer() {
        super(TypeElement.class);
    }

    @Override
    public void serialize(TypeElement typeElement,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        if (typeElement != null) {
            jsonGenerator.writeString(typeElement.getQualifiedName().toString());
        } else {
            jsonGenerator.writeNull();
        }
    }
}
