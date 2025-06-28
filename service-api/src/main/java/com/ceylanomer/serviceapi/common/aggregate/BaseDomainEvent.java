package com.ceylanomer.serviceapi.common.aggregate;

public interface BaseDomainEvent {
    String key();
    long version();
    String messageId();

}
