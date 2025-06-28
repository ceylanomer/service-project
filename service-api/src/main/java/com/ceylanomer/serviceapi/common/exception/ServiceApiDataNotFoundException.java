package com.ceylanomer.serviceapi.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceApiDataNotFoundException extends RuntimeException {
    private final String key;
    private final String[] args;

    public ServiceApiDataNotFoundException(String key) {
        super(key);
        this.key = key;
        args = new String[0];
    }

    public ServiceApiDataNotFoundException(String key, String... args) {
        super(key);
        this.key = key;
        this.args = args;
    }
}
