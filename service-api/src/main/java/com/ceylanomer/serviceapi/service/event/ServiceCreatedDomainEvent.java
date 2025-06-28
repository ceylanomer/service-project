package com.ceylanomer.serviceapi.service.event;

import com.ceylanomer.serviceapi.common.aggregate.DomainEventType;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.common.ServiceDomainEvent;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceCreatedDomainEvent extends ServiceDomainEvent {
    private List<Resource> resources;
    public ServiceCreatedDomainEvent(ServiceAggregate service) {
        super(service, DomainEventType.SERVICE_CREATED);
        this.resources = service.getResources();
    }
}
