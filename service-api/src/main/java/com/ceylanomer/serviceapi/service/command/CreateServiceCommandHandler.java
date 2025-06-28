package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.command.CommandHandler;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateServiceCommandHandler extends CommandHandler<CreateServiceCommand, ServiceAggregate> {
    private final ServiceRepository serviceRepository;

    @Override
    public ServiceAggregate handle(CreateServiceCommand command) {
        var service = serviceRepository.create(command.getResources());
        log.info("Service created with id: {}", service.getId());
        return service;
    }

}
