package com.ceylanomer.serviceapi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class LocalizationConfiguration implements WebMvcConfigurer {
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/commons", "i18n/serviceapi");
        source.setDefaultEncoding("UTF-8");
        Locale.setDefault(new Locale("en", "GB"));
        return source;
    }
}
