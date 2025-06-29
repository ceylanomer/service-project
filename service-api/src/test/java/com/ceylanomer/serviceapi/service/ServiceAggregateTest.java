package com.ceylanomer.serviceapi.service;

import com.ceylanomer.serviceapi.service.event.ServiceCreatedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceDeletedDomainEvent;
import com.ceylanomer.serviceapi.service.event.ServiceUpdatedDomainEvent;
import com.ceylanomer.serviceapi.service.persistence.Owner;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import com.ceylanomer.serviceapi.service.persistence.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServiceAggregate Unit Tests")
class ServiceAggregateTest {

    @Nested
    @DisplayName("Service Creation")
    class ServiceCreation {

        @Test
        @DisplayName("Should create service with resources and active status")
        void createService_WithResources_ShouldSetActiveStatusAndCreateEvent() {
            // Arrange
            String serviceId = "service-123";
            List<Resource> resources = createTestResources();

            // Act
            ServiceAggregate service = new ServiceAggregate(serviceId, resources);

            // Assert
            assertThat(service.getId()).isEqualTo(serviceId);
            assertThat(service.getResources()).isEqualTo(resources);
            assertThat(service.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(service.getVersion()).isEqualTo(1L);
            assertThat(service.getDomainEvents()).hasSize(1);
            assertThat(service.getDomainEvents().get(0)).isInstanceOf(ServiceCreatedDomainEvent.class);
        }

        @Test
        @DisplayName("Should create empty service with default constructor")
        void createService_WithDefaultConstructor_ShouldInitializeCorrectly() {
            // Act
            ServiceAggregate service = new ServiceAggregate();

            // Assert
            assertThat(service.getId()).isNull();
            assertThat(service.getResources()).isNull();
            assertThat(service.getStatus()).isNull();
            assertThat(service.getVersion()).isEqualTo(0L);
            assertThat(service.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Service Updates")
    class ServiceUpdates {

        @Test
        @DisplayName("Should update resources and create update event")
        void updateResources_WithNewResources_ShouldUpdateAndCreateEvent() {
            // Arrange
            ServiceAggregate service = new ServiceAggregate("service-123", createTestResources());
            List<Resource> newResources = createUpdatedTestResources();
            int initialEventCount = service.getDomainEvents().size();

            // Act
            service.updateResources(newResources);

            // Assert
            assertThat(service.getResources()).isEqualTo(newResources);
            assertThat(service.getVersion()).isEqualTo(2L);
            assertThat(service.getDomainEvents()).hasSize(initialEventCount + 1);
            assertThat(service.getDomainEvents().get(1)).isInstanceOf(ServiceUpdatedDomainEvent.class);
        }

        @Test
        @DisplayName("Should handle empty resources update")
        void updateResources_WithEmptyList_ShouldUpdateSuccessfully() {
            // Arrange
            ServiceAggregate service = new ServiceAggregate("service-123", createTestResources());
            List<Resource> emptyResources = List.of();

            // Act
            service.updateResources(emptyResources);

            // Assert
            assertThat(service.getResources()).isEmpty();
            assertThat(service.getVersion()).isEqualTo(2L);
            assertThat(service.getDomainEvents()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Service Deletion")
    class ServiceDeletion {

        @Test
        @DisplayName("Should mark service as deleted and create delete event")
        void delete_ActiveService_ShouldMarkAsDeletedAndCreateEvent() {
            // Arrange
            ServiceAggregate service = new ServiceAggregate("service-123", createTestResources());
            int initialEventCount = service.getDomainEvents().size();

            // Act
            service.delete();

            // Assert
            assertThat(service.getStatus()).isEqualTo(Status.DELETED);
            assertThat(service.getVersion()).isEqualTo(2L);
            assertThat(service.getDomainEvents()).hasSize(initialEventCount + 1);
            assertThat(service.getDomainEvents().get(1)).isInstanceOf(ServiceDeletedDomainEvent.class);
        }

        @Test
        @DisplayName("Should handle multiple delete operations")
        void delete_CalledMultipleTimes_ShouldRemainDeletedAndCreateMultipleEvents() {
            // Arrange
            ServiceAggregate service = new ServiceAggregate("service-123", createTestResources());

            // Act
            service.delete();
            service.delete();

            // Assert
            assertThat(service.getStatus()).isEqualTo(Status.DELETED);
            assertThat(service.getVersion()).isEqualTo(3L);
            assertThat(service.getDomainEvents()).hasSize(3);
            assertThat(service.getDomainEvents().get(1)).isInstanceOf(ServiceDeletedDomainEvent.class);
            assertThat(service.getDomainEvents().get(2)).isInstanceOf(ServiceDeletedDomainEvent.class);
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("Should handle complex workflow with multiple events")
        void complexWorkflow_CreateUpdateDelete_ShouldGenerateCorrectEvents() {
            // Arrange
            String serviceId = "service-123";
            List<Resource> initialResources = createTestResources();
            List<Resource> updatedResources = createUpdatedTestResources();

            // Act
            ServiceAggregate service = new ServiceAggregate(serviceId, initialResources);
            service.updateResources(updatedResources);
            service.delete();

            // Assert
            assertThat(service.getVersion()).isEqualTo(3L);
            assertThat(service.getDomainEvents()).hasSize(3);
            assertThat(service.getDomainEvents().get(0)).isInstanceOf(ServiceCreatedDomainEvent.class);
            assertThat(service.getDomainEvents().get(1)).isInstanceOf(ServiceUpdatedDomainEvent.class);
            assertThat(service.getDomainEvents().get(2)).isInstanceOf(ServiceDeletedDomainEvent.class);
        }

        @Test
        @DisplayName("Should support domain events manipulation")
        void withDomainEvents_ReplacingEvents_ShouldUpdateEventsList() {
            // Arrange
            ServiceAggregate service = new ServiceAggregate("service-123", createTestResources());
            var customEvents = List.of((com.ceylanomer.serviceapi.common.aggregate.DomainEvent) new ServiceCreatedDomainEvent(service));

            // Act
            ServiceAggregate result = service.withDomainEvents(customEvents);

            // Assert
            assertThat(result).isSameAs(service);
            assertThat(service.getDomainEvents()).isEqualTo(customEvents);
        }
    }

    // Test data factory methods
    private List<Resource> createTestResources() {
        Owner owner1 = new Owner("owner-1", "John Doe", "ACC001", 1);
        Owner owner2 = new Owner("owner-2", "Jane Smith", "ACC002", 2);
        
        Resource resource1 = new Resource("res-1", List.of(owner1));
        Resource resource2 = new Resource("res-2", List.of(owner1, owner2));
        
        return Arrays.asList(resource1, resource2);
    }

    private List<Resource> createUpdatedTestResources() {
        Owner owner3 = new Owner("owner-3", "Bob Johnson", "ACC003", 3);
        Resource resource3 = new Resource("res-3", List.of(owner3));
        
        return List.of(resource3);
    }
} 