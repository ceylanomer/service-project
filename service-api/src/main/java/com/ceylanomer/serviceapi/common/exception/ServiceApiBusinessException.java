package com.ceylanomer.serviceapi.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceApiBusinessException extends RuntimeException {
    private final String key;
    private final String[] args;

    public ServiceApiBusinessException(String key) {
        super(key);
        this.key = key;
        args = new String[0];
    }

    public ServiceApiBusinessException(String key, String... args) {
        super(key);
        this.key = key;
        this.args = args;
    }
}
