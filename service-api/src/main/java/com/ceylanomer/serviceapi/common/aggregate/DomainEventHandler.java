package com.ceylanomer.serviceapi.common.aggregate;

public abstract class DomainEventHandler<T extends DomainEvent>{
    protected abstract void handle(T event);
}
