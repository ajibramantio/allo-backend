package com.allo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Create a custom serializer for Double to avoid scientific notation
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new DoubleSerializer());
        module.addSerializer(double.class, new DoubleSerializer());
        objectMapper.registerModule(module);
        
        return objectMapper;
    }
    
    private static class DoubleSerializer extends JsonSerializer<Double> {
        private static final DecimalFormat df = new DecimalFormat("0.##########", 
            DecimalFormatSymbols.getInstance(Locale.US));
        
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) 
                throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                // Format as string to avoid scientific notation, then parse back to number
                String formatted = df.format(value);
                gen.writeNumber(formatted);
            }
        }
    }
}

