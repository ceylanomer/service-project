# Service Project

A microservice project consisting of a Spring Boot API service and a Go HTTP client service with bulk processing capabilities.

## Quick Start

```bash
# Start all services
./run-local.sh

# Stop services
./run-local.sh stop

# View status
./run-local.sh status
```

## Requirements

- Docker
- Docker Compose

## Services

### Service API (Spring Boot)
**Port:** 8080

REST API service for managing services with MongoDB persistence.

#### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/services` | Create a new service |
| GET | `/api/services/{id}` | Retrieve service by ID |
| PUT | `/api/services/{id}` | Update service |
| DELETE | `/api/services/{id}` | Delete service |
| GET | `/actuator/health` | Health check |

#### Example Usage

```bash
# Create service
curl -X POST http://localhost:8080/api/services \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Service", "description": "A test service"}'

# Get service by ID
curl http://localhost:8080/api/services/{service-id}

# Health check
curl http://localhost:8080/actuator/health
```

### Service Client (Go)
**Port:** 8081

HTTP client service that provides bulk service retrieval with configurable parallel processing.

#### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/services` | Create service (proxy to service-api) |
| GET | `/api/v1/services/batch` | Bulk retrieve services |
| GET | `/ready` | Readiness check |
| GET | `/live` | Liveness check |
| GET | `/metrics` | Prometheus metrics |

#### Bulk Retrieve Usage

```bash
# Single service
curl -X GET http://localhost:8081/api/v1/services/batch \
  -H "Content-Type: application/json" \
  -d '{"serviceId": "service-123"}'

# Multiple services (bulk)
curl -X GET http://localhost:8081/api/v1/services/batch \
  -H "Content-Type: application/json" \
  -d '{"serviceIds": ["service-1", "service-2", "service-3"]}'
```

#### Bulk Processing Configuration

Configuration files: `service-client/config/{env}/dynamicConfigs.json`

```json
{
  "numberOfParallelRequests": 100,
  "numberOfSteps": 10
}
```

- **numberOfParallelRequests**: Maximum concurrent requests per step
- **numberOfSteps**: Number of processing batches

#### Response Format

```json
{
  "summary": {
    "totalRequests": 100,
    "successfulCount": 95,
    "failedCount": 5,
    "processingTimeMs": 2150
  }
}
```

## Script Usage

The `run-local.sh` script provides simple service management:

```bash
./run-local.sh start     # Start all services
./run-local.sh stop      # Stop all services  
./run-local.sh restart   # Restart all services
./run-local.sh status    # Show service status
./run-local.sh logs      # View service logs
./run-local.sh test      # Test service endpoints
./run-local.sh clean     # Remove all containers and volumes
```

## Architecture

- **MongoDB**: Database (port 27017)
- **Service API**: Spring Boot REST API (port 8080)
- **Service Client**: Go HTTP client with bulk processing (port 8081)

All services communicate through Docker network and are managed via Docker Compose.

## Environment Configuration

Services support multiple environments:
- `dev`: Development configuration
- `docker`: Docker environment configuration  
- `prod`: Production configuration

Environment is set via `SPRING_PROFILES_ACTIVE` (service-api) and `ACTIVE_PROFILE` (service-client). 