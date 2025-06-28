package com.ceylanomer.serviceapi.service.common;

import com.ceylanomer.serviceapi.common.aggregate.BaseAggregate;
import com.ceylanomer.serviceapi.common.aggregate.DomainEvent;
import com.ceylanomer.serviceapi.common.aggregate.DomainEventType;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.event.ServiceCreatedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceDeletedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceUpdatedDomainEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServiceCreatedDomainEvent.class, name = "SERVICE_CREATED"),
        @JsonSubTypes.Type(value = ServiceUpdatedDomainEvent.class, name = "SERVICE_UPDATED"),
        @JsonSubTypes.Type(value = ServiceDeletedDomainEvent.class, name = "SERVICE_DELETED")
})
public class ServiceDomainEvent extends DomainEvent {
    public ServiceDomainEvent(String id, Long version, DomainEventType type) {
        super(id, version, type);
    }

    public ServiceDomainEvent(BaseAggregate<ServiceAggregate> aggregate, DomainEventType type) {
        super(aggregate, type);
    }

    public ServiceDomainEvent() {
    }
}
