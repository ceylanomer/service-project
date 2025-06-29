package com.ceylanomer.serviceapi.service.persistence;

import com.ceylanomer.serviceapi.common.exception.ServiceApiDataNotFoundException;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@DisplayName("ServiceRepository Integration Tests")
class ServiceRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ServiceMongoRepository serviceMongoRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @BeforeEach
    void setUp() {
        serviceMongoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create Service")
    class CreateService {

        @Test
        @DisplayName("Should create service with resources successfully")
        void create_WithValidResources_ShouldPersistAndReturnAggregate() {
            // Arrange
            List<Resource> resources = createTestResources();

            // Act
            ServiceAggregate result = serviceRepository.create(resources);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getResources()).isEqualTo(resources);
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);

            // Verify persistence
            Optional<ServiceDocument> persisted = serviceMongoRepository.findById(result.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getResources()).isEqualTo(resources);
            assertThat(persisted.get().getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should create service with empty resources")
        void create_WithEmptyResources_ShouldSucceed() {
            // Arrange
            List<Resource> emptyResources = List.of();

            // Act
            ServiceAggregate result = serviceRepository.create(emptyResources);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getResources()).isEmpty();
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Retrieve Service")
    class RetrieveService {

        @Test
        @DisplayName("Should retrieve existing service by ID")
        void retrieveServiceById_WithExistingId_ShouldReturnAggregate() {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceAggregate created = serviceRepository.create(resources);

            // Act
            ServiceAggregate retrieved = serviceRepository.retrieveServiceById(created.getId());

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getResources()).isEqualTo(resources);
            assertThat(retrieved.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when service not found")
        void retrieveServiceById_WithNonExistentId_ShouldThrowException() {
            // Arrange
            String nonExistentId = "non-existent-id";

            // Act & Assert
            assertThatThrownBy(() -> serviceRepository.retrieveServiceById(nonExistentId))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }
    }

    @Nested
    @DisplayName("Update Service")
    class UpdateService {

        @Test
        @DisplayName("Should update existing service successfully")
        void update_WithExistingService_ShouldUpdateAndReturnAggregate() {
            // Arrange
            List<Resource> initialResources = createTestResources();
            ServiceAggregate created = serviceRepository.create(initialResources);
            
            List<Resource> updatedResources = createUpdatedTestResources();
            created.setResources(updatedResources);

            // Act
            ServiceAggregate updated = serviceRepository.update(created);

            // Assert
            assertThat(updated).isNotNull();
            assertThat(updated.getId()).isEqualTo(created.getId());
            assertThat(updated.getResources()).isEqualTo(updatedResources);

            // Verify persistence
            Optional<ServiceDocument> persisted = serviceMongoRepository.findById(created.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getResources()).isEqualTo(updatedResources);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent service")
        void update_WithNonExistentService_ShouldThrowException() {
            // Arrange
            ServiceAggregate nonExistent = new ServiceAggregate();
            nonExistent.setId("non-existent-id");
            nonExistent.setResources(createTestResources());

            // Act & Assert
            assertThatThrownBy(() -> serviceRepository.update(nonExistent))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }
    }

    @Nested
    @DisplayName("Delete Service")
    class DeleteService {

        @Test
        @DisplayName("Should soft delete existing service")
        void delete_WithExistingService_ShouldMarkAsDeleted() {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceAggregate created = serviceRepository.create(resources);

            // Act
            serviceRepository.delete(created.getId());

            // Assert
            Optional<ServiceDocument> persisted = serviceMongoRepository.findById(created.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getStatus()).isEqualTo(Status.DELETED);
            assertThat(persisted.get().getResources()).isEqualTo(resources); // Resources should remain unchanged
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent service")
        void delete_WithNonExistentId_ShouldThrowException() {
            // Arrange
            String nonExistentId = "non-existent-id";

            // Act & Assert
            assertThatThrownBy(() -> serviceRepository.delete(nonExistentId))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle concurrent reads and writes")
        void concurrentOperations_MultipleThreads_ShouldMaintainDataIntegrity() throws InterruptedException {
            // Arrange
            List<Resource> resources = createTestResources();
            ServiceAggregate created = serviceRepository.create(resources);

            // Act
            Thread readThread = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    serviceRepository.retrieveServiceById(created.getId());
                }
            });

            Thread updateThread = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    ServiceAggregate service = serviceRepository.retrieveServiceById(created.getId());
                    List<Resource> newResources = createUpdatedTestResources();
                    service.setResources(newResources);
                    serviceRepository.update(service);
                }
            });

            readThread.start();
            updateThread.start();

            readThread.join();
            updateThread.join();

            // Assert
            ServiceAggregate finalService = serviceRepository.retrieveServiceById(created.getId());
            assertThat(finalService).isNotNull();
            assertThat(finalService.getId()).isEqualTo(created.getId());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle service with null owner in resources")
        void create_WithResourcesContainingNullOwner_ShouldHandleGracefully() {
            // Arrange
            Resource resourceWithNullOwner = new Resource("res-null", null);
            List<Resource> resources = List.of(resourceWithNullOwner);

            // Act
            ServiceAggregate result = serviceRepository.create(resources);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(1);
            assertThat(result.getResources().get(0).getOwners()).isNull();
        }

        @Test
        @DisplayName("Should handle service with very large resource list")
        void create_WithLargeResourceList_ShouldPersistSuccessfully() {
            // Arrange
            List<Resource> largeResourceList = createLargeResourceList(100);

            // Act
            ServiceAggregate result = serviceRepository.create(largeResourceList);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(100);

            // Verify retrieval
            ServiceAggregate retrieved = serviceRepository.retrieveServiceById(result.getId());
            assertThat(retrieved.getResources()).hasSize(100);
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

    private List<Resource> createLargeResourceList(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Owner owner = new Owner("owner-" + i, "Owner " + i, "ACC" + String.format("%03d", i), i % 5);
                    return new Resource("res-" + i, List.of(owner));
                })
                .toList();
    }
} 