package com.ceylanomer.serviceapi.service.rest.request;

import com.ceylanomer.serviceapi.service.command.CreateServiceCommand;
import com.ceylanomer.serviceapi.service.command.UpdateServiceCommand;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {
    private List<Resource> resources;
    public CreateServiceCommand toCreateCommand(){
        return CreateServiceCommand.builder()
                .resources(this.resources)
                .build();
    }

    public UpdateServiceCommand toUpdateCommand(String id){
        return UpdateServiceCommand.builder()
                .id(id)
                .resources(this.resources)
                .build();
    }
}
