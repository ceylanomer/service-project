package com.ceylanomer.serviceapi.common.aggregate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class DomainEvent implements BaseDomainEvent {
    private Header header;
    private String id;
    private DomainEventType type;

    public DomainEvent(BaseAggregate aggregate, DomainEventType type) {
        this.id = aggregate.getId();
        this.type = type;
        this.header = new Header(aggregate.getVersion());
    }

    public DomainEvent(String id, Long version, DomainEventType type) {
        this.id = id;
        this.type = type;
        this.header = new Header(version);
    }

    @Override
    public String key() {
        return String.valueOf(id);
    }

    @Override
    public long version() {
        return header.getVersion();
    }

    @Override
    public String messageId() {
        return header.getMessageId();
    }
}
