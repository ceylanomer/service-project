package com.ceylanomer.serviceapi.service.rest;

import com.ceylanomer.serviceapi.common.command.CommandBus;
import com.ceylanomer.serviceapi.common.controller.BaseController;
import com.ceylanomer.serviceapi.common.query.QueryBus;
import com.ceylanomer.serviceapi.common.response.Response;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.command.DeleteServiceCommand;
import com.ceylanomer.serviceapi.service.query.GetServiceByIdQuery;
import com.ceylanomer.serviceapi.service.rest.request.ServiceRequest;
import com.ceylanomer.serviceapi.service.rest.response.ServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController extends BaseController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @GetMapping("/{id}")
    public Response<ServiceResponse> getById(@PathVariable String id) {
        ServiceAggregate serviceAggregate = queryBus.execute(GetServiceByIdQuery.builder().id(id).build());
        return respond(ServiceResponse.from(serviceAggregate));
    }

    @PostMapping
    public Response<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        ServiceAggregate response = commandBus.executeWithResponse(request.toCreateCommand());
        return respond(ServiceResponse.from(response));
    }

    @PutMapping("/{id}")
    public Response<ServiceResponse> update(@PathVariable String id, @Valid @RequestBody ServiceRequest request) {
        ServiceAggregate service = commandBus.executeWithResponse(request.toUpdateCommand(id));
        return respond(ServiceResponse.from(service));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        commandBus.executeWithResponse(DeleteServiceCommand.builder().id(id).build());
        log.info("Service with id {} deleted successfully", id);
    }
}
