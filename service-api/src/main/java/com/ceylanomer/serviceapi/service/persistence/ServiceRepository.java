package com.ceylanomer.serviceapi.service.persistence;

import com.ceylanomer.serviceapi.common.exception.ServiceApiDataNotFoundException;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
@RequiredArgsConstructor
public class ServiceRepository {
    private final ServiceMongoRepository serviceMongoRepository;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Cacheable(value = "services", key = "#id")
    public ServiceAggregate retrieveServiceById(String id) {
        lock.writeLock().lock();
        try {
            return serviceMongoRepository.findById(id).orElseThrow(() -> new ServiceApiDataNotFoundException("common.client.noSuchElement")).toAggregate();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ServiceAggregate create(List<Resource> resources) {
        lock.writeLock().lock();
        try {
            var serviceDoc = serviceMongoRepository.save(ServiceDocument.builder()
                    .resources(resources)
                    .status(Status.ACTIVE)
                    .build());
            return new ServiceAggregate(serviceDoc.getId(), serviceDoc.getResources());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @CachePut(value = "service", key = "#service.id")
    public ServiceAggregate update(ServiceAggregate service) {
        lock.writeLock().lock();
        try {
            var serviceDoc = serviceMongoRepository.findById(service.getId())
                    .orElseThrow(() -> new ServiceApiDataNotFoundException("common.client.noSuchElement"));
            serviceDoc.setResources(service.getResources());
            serviceMongoRepository.save(serviceDoc);
            return serviceDoc.toAggregate();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @CacheEvict(value = "service", key = "#id")
    public void delete(String id) {
        lock.writeLock().lock();
        try {
            var serviceDoc = serviceMongoRepository.findById(id)
                    .orElseThrow(() -> new ServiceApiDataNotFoundException("common.client.noSuchElement"));
            serviceDoc.setStatus(Status.DELETED);
            serviceMongoRepository.save(serviceDoc);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
