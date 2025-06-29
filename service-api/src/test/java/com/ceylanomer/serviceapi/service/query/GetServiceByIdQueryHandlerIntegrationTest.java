package com.ceylanomer.serviceapi.service.query;

import com.ceylanomer.serviceapi.common.exception.ServiceApiDataNotFoundException;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.Owner;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import com.ceylanomer.serviceapi.service.persistence.ServiceDocument;
import com.ceylanomer.serviceapi.service.persistence.ServiceMongoRepository;
import com.ceylanomer.serviceapi.service.persistence.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@DisplayName("GetServiceByIdQueryHandler Integration Tests")
class GetServiceByIdQueryHandlerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private GetServiceByIdQueryHandler queryHandler;

    @Autowired
    private ServiceMongoRepository serviceMongoRepository;

    @BeforeEach
    void setUp() {
        serviceMongoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Get Service By ID")
    class GetServiceById {

        @Test
        @DisplayName("Should retrieve existing service successfully")
        void handle_WithExistingServiceId_ShouldReturnServiceAggregate() {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(resources)
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedDocument.getId());
            assertThat(result.getResources()).isEqualTo(resources);
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when service not found")
        void handle_WithNonExistentServiceId_ShouldThrowException() {
            // Arrange
            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id("non-existent-id")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> queryHandler.handle(query))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }

        @Test
        @DisplayName("Should retrieve service with empty resources")
        void handle_WithServiceHavingEmptyResources_ShouldReturnCorrectly() {
            // Arrange
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(List.of())
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).isEmpty();
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should retrieve deleted service")
        void handle_WithDeletedService_ShouldReturnServiceWithDeletedStatus() {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(resources)
                            .status(Status.DELETED)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedDocument.getId());
            assertThat(result.getStatus()).isEqualTo(Status.DELETED);
            assertThat(result.getResources()).isEqualTo(resources);
        }

        @Test
        @DisplayName("Should retrieve service with complex resource hierarchy")
        void handle_WithComplexResources_ShouldReturnCompleteHierarchy() {
            // Arrange
            List<Resource> complexResources = createComplexTestResources();
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(complexResources)
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(2);
            assertThat(result.getResources().get(0).getOwners()).hasSize(3);
            assertThat(result.getResources().get(1).getOwners()).hasSize(1);
            
            // Verify resource details
            Resource firstResource = result.getResources().get(0);
            assertThat(firstResource.getId()).isEqualTo("res-complex-1");
            assertThat(firstResource.getOwners().get(0).getName()).isEqualTo("Alice Cooper");
            assertThat(firstResource.getOwners().get(1).getName()).isEqualTo("Bob Dylan");
            assertThat(firstResource.getOwners().get(2).getName()).isEqualTo("Charlie Parker");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null resource owners")
        void handle_WithNullResourceOwners_ShouldHandleGracefully() {
            // Arrange
            Resource resourceWithNullOwners = new Resource("res-null", null);
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(List.of(resourceWithNullOwners))
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(1);
            assertThat(result.getResources().get(0).getOwners()).isNull();
        }

        @Test
        @DisplayName("Should handle service with many resources")
        void handle_WithLargeResourceSet_ShouldReturnAllResources() {
            // Arrange
            List<Resource> largeResourceSet = createLargeResourceSet(50);
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(largeResourceSet)
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act
            ServiceAggregate result = queryHandler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(50);
            assertThat(result.getResources().get(0).getId()).isEqualTo("res-0");
            assertThat(result.getResources().get(49).getId()).isEqualTo("res-49");
        }

        @Test
        @DisplayName("Should handle concurrent query operations")
        void handle_ConcurrentQueries_ShouldReturnConsistentResults() throws InterruptedException {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(resources)
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act - Concurrent queries
            Thread[] threads = new Thread[10];
            ServiceAggregate[] results = new ServiceAggregate[10];

            for (int i = 0; i < 10; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = queryHandler.handle(query);
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // Assert - All results should be consistent
            for (ServiceAggregate result : results) {
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(savedDocument.getId());
                assertThat(result.getResources()).isEqualTo(resources);
                assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
            }
        }
    }

    @Nested
    @DisplayName("Query Handler Performance")
    class QueryHandlerPerformance {

        @Test
        @DisplayName("Should handle multiple sequential queries efficiently")
        void handle_MultipleSequentialQueries_ShouldPerformWell() {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceDocument savedDocument = serviceMongoRepository.save(
                    ServiceDocument.builder()
                            .resources(resources)
                            .status(Status.ACTIVE)
                            .build()
            );

            GetServiceByIdQuery query = GetServiceByIdQuery.builder()
                    .id(savedDocument.getId())
                    .build();

            // Act - Multiple queries
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                ServiceAggregate result = queryHandler.handle(query);
                assertThat(result).isNotNull();
            }
            long endTime = System.currentTimeMillis();

            // Assert - Should complete reasonably quickly (basic performance check)
            long executionTime = endTime - startTime;
            assertThat(executionTime).isLessThan(5000); // Less than 5 seconds for 100 queries
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

    private List<Resource> createComplexTestResources() {
        Owner owner1 = new Owner("owner-1", "Alice Cooper", "ACC001", 1);
        Owner owner2 = new Owner("owner-2", "Bob Dylan", "ACC002", 2);
        Owner owner3 = new Owner("owner-3", "Charlie Parker", "ACC003", 3);
        Owner owner4 = new Owner("owner-4", "Diana Ross", "ACC004", 4);
        
        Resource resource1 = new Resource("res-complex-1", List.of(owner1, owner2, owner3));
        Resource resource2 = new Resource("res-complex-2", List.of(owner4));
        
        return List.of(resource1, resource2);
    }

    private List<Resource> createLargeResourceSet(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Owner owner = new Owner("owner-" + i, "Owner " + i, "ACC" + String.format("%03d", i), i % 5);
                    return new Resource("res-" + i, List.of(owner));
                })
                .toList();
    }
} 