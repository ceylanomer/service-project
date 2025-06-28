package com.ceylanomer.serviceapi.service.query;

import com.ceylanomer.serviceapi.common.query.QueryHandler;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetServiceByIdQueryHandler implements QueryHandler<GetServiceByIdQuery, ServiceAggregate> {

    private final ServiceRepository serviceRepository;

    @Override
    public ServiceAggregate handle(GetServiceByIdQuery query) {
        var service = serviceRepository.retrieveServiceById(query.getId());
        return service;
    }
}
