package com.ceylanomer.serviceapi.service.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceMongoRepository extends MongoRepository<ServiceDocument, String> {
}
