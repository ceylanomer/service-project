package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.command.CommandHandler;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateServiceCommandHandler extends CommandHandler<UpdateServiceCommand, ServiceAggregate> {
    private final ServiceRepository serviceRepository;

    @Override
    public ServiceAggregate handle(UpdateServiceCommand command) {
        var service = serviceRepository.retrieveServiceById(command.getId());
        service.updateResources(command.getResources());
        serviceRepository.update(service);
        log.info("Service updated with id: {}", service.getId());
        return service;
    }

}
