package com.ceylanomer.serviceapi.common.command;

import com.ceylanomer.serviceapi.common.aggregate.BaseAggregate;
import com.ceylanomer.serviceapi.common.aggregate.DomainEvent;
import com.ceylanomer.serviceapi.common.aggregate.DomainEventPublisher;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Setter
@Slf4j
public abstract class CommandHandler<C extends Command, R extends BaseAggregate> {

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    protected abstract R handle(C command);

    public R process(C command) {
        return handleCommand(command);
    }

    protected R handleCommand(C command) {
        var aggregate = handle(command);
        aggregate.getDomainEvents().forEach(event -> {
            if (event instanceof DomainEvent) {
                domainEventPublisher.publish((DomainEvent) event);
            }
        });
        return aggregate;
    }
}
