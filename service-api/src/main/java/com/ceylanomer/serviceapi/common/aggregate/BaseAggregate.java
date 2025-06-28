package com.ceylanomer.serviceapi.common.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
@AllArgsConstructor
public class BaseAggregate <T extends BaseAggregate<T>>{
    private String id;
    private Long version;
    private Date createdDate;
    private Date lastModifiedDate;
    private List<DomainEvent> domainEvents;

    public BaseAggregate() {
        version = 0L;
        domainEvents = new ArrayList<>();
    }
    public BaseAggregate(String id) {
        this();
        this.id = id;
    }

    public void addDomainEvent(Supplier<DomainEvent> supplier) {
        if (Objects.isNull(this.version))
            this.version = 0L;
        this.version++;

        if (this.domainEvents == null)
            this.domainEvents = new ArrayList<>();

        domainEvents.add(supplier.get());
    }

    public T withDomainEvents(List<DomainEvent> domainEvents) {
        this.domainEvents = domainEvents;
        return (T) this;
    }
}
