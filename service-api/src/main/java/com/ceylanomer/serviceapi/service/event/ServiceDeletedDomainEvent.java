package com.ceylanomer.serviceapi.service.event;

import com.ceylanomer.serviceapi.common.aggregate.DomainEventType;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.common.ServiceDomainEvent;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import com.ceylanomer.serviceapi.service.persistence.Status;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceDeletedDomainEvent extends ServiceDomainEvent {
    private String id;
    private Status status;
    public ServiceDeletedDomainEvent(ServiceAggregate service) {
        super(service, DomainEventType.SERVICE_DELETED);
        this.status = service.getStatus();
    }
}
