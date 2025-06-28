package com.ceylanomer.serviceapi.service;

import com.ceylanomer.serviceapi.common.aggregate.BaseAggregate;
import com.ceylanomer.serviceapi.service.event.ServiceCreatedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceDeletedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceUpdatedDomainEvent;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import com.ceylanomer.serviceapi.service.persistence.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceAggregate extends BaseAggregate<ServiceAggregate> {
    private List<Resource> resources;
    private Status status;

    public ServiceAggregate() {
    }

    public ServiceAggregate(String id, List<Resource> resources) {
        super(id);
        this.resources = resources;
        this.status = Status.ACTIVE;
        addDomainEvent(() -> new ServiceCreatedDomainEvent(this));
    }

    public void updateResources(List<Resource> resources) {
        this.resources = resources;
        addDomainEvent(() -> new ServiceUpdatedDomainEvent(this));
    }

    public void delete() {
        this.status = Status.DELETED;
        addDomainEvent(() -> new ServiceDeletedDomainEvent(this));
    }
}
