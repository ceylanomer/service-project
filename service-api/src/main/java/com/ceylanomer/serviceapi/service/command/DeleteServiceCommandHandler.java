package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.command.CommandHandler;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteServiceCommandHandler extends CommandHandler<DeleteServiceCommand, ServiceAggregate> {
    private final ServiceRepository serviceRepository;

    @Override
    public ServiceAggregate handle(DeleteServiceCommand command) {
        ServiceAggregate service = serviceRepository.retrieveServiceById(command.getId());
        service.delete();
        log.info("Deleting service with id: {}", command.getId());
        serviceRepository.delete(service.getId());
        return service;
    }

}
