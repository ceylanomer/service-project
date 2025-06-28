package com.ceylanomer.serviceapi.service.persistence;

import com.ceylanomer.serviceapi.service.ServiceAggregate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "services")
public class ServiceDocument {
    @Id
    private String id;
    @Field(value = "resources")
    private List<Resource> resources;
    @Field(value = "status")
    private Status status;

    public ServiceAggregate toAggregate() {
        var doc = new ServiceAggregate();
        doc.setId(this.id);
        doc.setResources(this.resources);
        doc.setStatus(this.status);
        return doc;
    }
}
