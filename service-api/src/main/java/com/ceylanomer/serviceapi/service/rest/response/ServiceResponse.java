package com.ceylanomer.serviceapi.service.rest.response;

import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceResponse {
    private String id;
    private List<Resource> resources;
    public static ServiceResponse from(ServiceAggregate serviceAggregate){
        return ServiceResponse.builder()
                .id(serviceAggregate.getId())
                .resources(serviceAggregate.getResources())
                .build();
    }
}
