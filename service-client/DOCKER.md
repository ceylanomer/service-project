# Docker Setup for Service Client

This document provides instructions for running the service-client application using Docker.

## Prerequisites

- Docker installed and running
- Docker Compose (optional, for easier management)

## Quick Start

### Option 1: Using the Build Script (Recommended)

The project includes a convenient build script that handles building and running the container:

```bash
# Build the Docker image
./docker-build.sh build

# Run the container
./docker-build.sh run

# View logs
./docker-build.sh logs

# Stop the container
./docker-build.sh stop

# Restart (rebuild and run)
./docker-build.sh restart
```

### Option 2: Using Docker Commands Directly

```bash
# Build the image
docker build -t service-client:latest .

# Run the container
docker run -d \
  --name service-client \
  -p 8081:8081 \
  -e ACTIVE_PROFILE=docker \
  -v $(pwd)/config:/config:ro \
  service-client:latest

# View logs
docker logs -f service-client

# Stop the container
docker stop service-client
```

### Option 3: Using Docker Compose

```bash
# Start the service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

## Application Endpoints

Once the container is running, the following endpoints are available:

- **Application**: http://localhost:8081
- **Health Check**: http://localhost:8081/live
- **Readiness Check**: http://localhost:8081/ready
- **Metrics**: http://localhost:8081/metrics
- **API**: http://localhost:8081/api/v1/services

## Configuration

The application uses environment-specific configuration files:

- `config/application-docker.yaml` - Docker environment configuration
- `config/docker/dynamicConfigs.json` - Dynamic configuration for Docker

Make sure the `ACTIVE_PROFILE=docker` environment variable is set to load the correct configuration.

## Docker Image Details

- **Base Image**: Built using multi-stage build with `golang:1.24.0-alpine` for building and `scratch` for runtime
- **Size**: Minimal footprint using scratch base image
- **Security**: Runs as non-root user (UID 65534)
- **Port**: Exposes port 8081
- **Health**: Built-in health endpoints at `/live` and `/ready`

## Troubleshooting

### Check if the container is running
```bash
docker ps | grep service-client
```

### View container logs
```bash
docker logs service-client
```

### Access container shell (if needed for debugging)
Note: Since we use a scratch image, there's no shell available. For debugging, you may need to temporarily change the Dockerfile to use `alpine` instead of `scratch`.

### Port conflicts
If port 8081 is already in use, modify the port mapping:
```bash
docker run -p 8082:8081 service-client:latest
```

## Development Notes

- The Docker image is optimized for production with minimal size and security
- Configuration files are mounted as read-only volumes
- The application supports graceful shutdown on SIGTERM
- Observability features (tracing, metrics, logging) are enabled by default 