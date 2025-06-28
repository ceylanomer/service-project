package com.ceylanomer.serviceapi.service.event;

import com.ceylanomer.serviceapi.common.aggregate.DomainEventHandler;
import com.ceylanomer.serviceapi.service.common.ServiceDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceDomainEventHandler extends DomainEventHandler<ServiceDomainEvent> {
    @Override
    protected void handle(ServiceDomainEvent event) {
        log.info("Service Domain Event created: {}", event);
    }
}
