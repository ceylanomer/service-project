package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.exception.ServiceApiDataNotFoundException;
import com.ceylanomer.serviceapi.service.ServiceAggregate;
import com.ceylanomer.serviceapi.service.persistence.Owner;
import com.ceylanomer.serviceapi.service.persistence.Resource;
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
@DisplayName("Service Command Handlers Integration Tests")
class ServiceCommandHandlersIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CreateServiceCommandHandler createServiceCommandHandler;

    @Autowired
    private UpdateServiceCommandHandler updateServiceCommandHandler;

    @Autowired
    private DeleteServiceCommandHandler deleteServiceCommandHandler;

    @Autowired
    private ServiceMongoRepository serviceMongoRepository;

    @BeforeEach
    void setUp() {
        serviceMongoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create Service Command Handler")
    class CreateServiceCommandHandlerTests {

        @Test
        @DisplayName("Should create service successfully with valid command")
        void handle_WithValidCreateCommand_ShouldCreateService() {
            // Arrange
            List<Resource> resources = createTestResources();
            CreateServiceCommand command = CreateServiceCommand.builder()
                    .resources(resources)
                    .build();

            // Act
            ServiceAggregate result = createServiceCommandHandler.handle(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getResources()).isEqualTo(resources);
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(result.getDomainEvents()).hasSize(1);

            // Verify persistence
            var persisted = serviceMongoRepository.findById(result.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should create service with empty resources")
        void handle_WithEmptyResources_ShouldCreateServiceSuccessfully() {
            // Arrange
            CreateServiceCommand command = CreateServiceCommand.builder()
                    .resources(List.of())
                    .build();

            // Act
            ServiceAggregate result = createServiceCommandHandler.handle(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).isEmpty();
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should create service with complex resource hierarchy")
        void handle_WithComplexResources_ShouldCreateServiceSuccessfully() {
            // Arrange
            List<Resource> complexResources = createComplexTestResources();
            CreateServiceCommand command = CreateServiceCommand.builder()
                    .resources(complexResources)
                    .build();

            // Act
            ServiceAggregate result = createServiceCommandHandler.handle(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getResources()).hasSize(2);
            assertThat(result.getResources().get(0).getOwners()).hasSize(3);
            assertThat(result.getResources().get(1).getOwners()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update Service Command Handler")
    class UpdateServiceCommandHandlerTests {

        @Test
        @DisplayName("Should update existing service successfully")
        void handle_WithValidUpdateCommand_ShouldUpdateService() {
            // Arrange
            List<Resource> initialResources = createTestResources();
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(initialResources).build()
            );

            List<Resource> updatedResources = createUpdatedTestResources();
            UpdateServiceCommand command = UpdateServiceCommand.builder()
                    .id(created.getId())
                    .resources(updatedResources)
                    .build();

            // Act
            ServiceAggregate result = updateServiceCommandHandler.handle(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(created.getId());
            assertThat(result.getResources()).isEqualTo(updatedResources);
            assertThat(result.getDomainEvents()).hasSize(1); // Create + Update events

            // Verify persistence
            var persisted = serviceMongoRepository.findById(result.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getResources()).isEqualTo(updatedResources);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent service")
        void handle_WithNonExistentServiceId_ShouldThrowException() {
            // Arrange
            UpdateServiceCommand command = UpdateServiceCommand.builder()
                    .id("non-existent-id")
                    .resources(createTestResources())
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> updateServiceCommandHandler.handle(command))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }

        @Test
        @DisplayName("Should update service to empty resources")
        void handle_WithEmptyResourcesUpdate_ShouldUpdateSuccessfully() {
            // Arrange
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(createTestResources()).build()
            );

            UpdateServiceCommand command = UpdateServiceCommand.builder()
                    .id(created.getId())
                    .resources(List.of())
                    .build();

            // Act
            ServiceAggregate result = updateServiceCommandHandler.handle(command);

            // Assert
            assertThat(result.getResources()).isEmpty();
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Delete Service Command Handler")
    class DeleteServiceCommandHandlerTests {

        @Test
        @DisplayName("Should delete existing service successfully")
        void handle_WithValidDeleteCommand_ShouldDeleteService() {
            // Arrange
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(createTestResources()).build()
            );

            DeleteServiceCommand command = DeleteServiceCommand.builder()
                    .id(created.getId())
                    .build();

            // Act
            ServiceAggregate result = deleteServiceCommandHandler.handle(command);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(created.getId());
            assertThat(result.getStatus()).isEqualTo(Status.DELETED);
            assertThat(result.getDomainEvents()).hasSize(1); // Create + Delete events

            // Verify persistence (soft delete)
            var persisted = serviceMongoRepository.findById(result.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getStatus()).isEqualTo(Status.DELETED);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent service")
        void handle_WithNonExistentServiceId_ShouldThrowException() {
            // Arrange
            DeleteServiceCommand command = DeleteServiceCommand.builder()
                    .id("non-existent-id")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> deleteServiceCommandHandler.handle(command))
                    .isInstanceOf(ServiceApiDataNotFoundException.class)
                    .hasMessage("common.client.noSuchElement");
        }

        @Test
        @DisplayName("Should handle deletion of already deleted service")
        void handle_WithAlreadyDeletedService_ShouldHandleGracefully() {
            // Arrange
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(createTestResources()).build()
            );

            DeleteServiceCommand command = DeleteServiceCommand.builder()
                    .id(created.getId())
                    .build();

            // First deletion
            deleteServiceCommandHandler.handle(command);

            // Act - Second deletion
            ServiceAggregate result = deleteServiceCommandHandler.handle(command);

            // Assert
            assertThat(result.getStatus()).isEqualTo(Status.DELETED);
            assertThat(result.getDomainEvents()).hasSize(1); // Create + Delete + Delete events
        }
    }

    @Nested
    @DisplayName("Command Handler Integration")
    class CommandHandlerIntegration {

        @Test
        @DisplayName("Should handle complete service lifecycle")
        void serviceLifecycle_CreateUpdateDelete_ShouldWorkEndToEnd() {
            // Arrange & Act - Create
            List<Resource> initialResources = createTestResources();
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(initialResources).build()
            );

            // Act - Update
            List<Resource> updatedResources = createUpdatedTestResources();
            ServiceAggregate updated = updateServiceCommandHandler.handle(
                    UpdateServiceCommand.builder()
                            .id(created.getId())
                            .resources(updatedResources)
                            .build()
            );

            // Act - Delete
            ServiceAggregate deleted = deleteServiceCommandHandler.handle(
                    DeleteServiceCommand.builder().id(created.getId()).build()
            );

            // Assert
            assertThat(created.getId()).isEqualTo(updated.getId()).isEqualTo(deleted.getId());
            assertThat(created.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(updated.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(deleted.getStatus()).isEqualTo(Status.DELETED);
            
            assertThat(created.getResources()).isEqualTo(initialResources);
            assertThat(updated.getResources()).isEqualTo(updatedResources);
            assertThat(deleted.getResources()).isEqualTo(updatedResources); // Resources remain from last update

            // Verify final persistence state
            var persisted = serviceMongoRepository.findById(deleted.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getStatus()).isEqualTo(Status.DELETED);
        }

        @Test
        @DisplayName("Should handle concurrent command operations")
        void concurrentOperations_MultipleCommands_ShouldMaintainDataIntegrity() throws InterruptedException {
            // Arrange
            ServiceAggregate created = createServiceCommandHandler.handle(
                    CreateServiceCommand.builder().resources(createTestResources()).build()
            );

            // Act - Concurrent updates
            Thread updateThread1 = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    updateServiceCommandHandler.handle(
                            UpdateServiceCommand.builder()
                                    .id(created.getId())
                                    .resources(createUpdatedTestResources())
                                    .build()
                    );
                }
            });

            Thread updateThread2 = new Thread(() -> {
                for (int i = 0; i < 3; i++) {
                    updateServiceCommandHandler.handle(
                            UpdateServiceCommand.builder()
                                    .id(created.getId())
                                    .resources(createTestResources())
                                    .build()
                    );
                }
            });

            updateThread1.start();
            updateThread2.start();

            updateThread1.join();
            updateThread2.join();

            // Assert - Service should still exist and be consistent
            var persisted = serviceMongoRepository.findById(created.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getStatus()).isEqualTo(Status.ACTIVE);
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

    private List<Resource> createComplexTestResources() {
        Owner owner1 = new Owner("owner-1", "Alice Cooper", "ACC001", 1);
        Owner owner2 = new Owner("owner-2", "Bob Dylan", "ACC002", 2);
        Owner owner3 = new Owner("owner-3", "Charlie Parker", "ACC003", 3);
        Owner owner4 = new Owner("owner-4", "Diana Ross", "ACC004", 4);
        
        Resource resource1 = new Resource("res-complex-1", List.of(owner1, owner2, owner3));
        Resource resource2 = new Resource("res-complex-2", List.of(owner4));
        
        return List.of(resource1, resource2);
    }
} 