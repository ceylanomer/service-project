#!/bin/bash

# Docker build and run script for service-client

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="service-client"
IMAGE_TAG="latest"
CONTAINER_NAME="service-client"
HOST_PORT="8081"
CONTAINER_PORT="8081"

# Functions
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Build the Docker image
build_image() {
    print_status "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
    docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .
    print_status "Docker image built successfully!"
}

# Run the container
run_container() {
    print_status "Starting container: ${CONTAINER_NAME}"
    
    # Stop and remove existing container if it exists
    if docker ps -a --format 'table {{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        print_warning "Stopping and removing existing container: ${CONTAINER_NAME}"
        docker stop "${CONTAINER_NAME}" || true
        docker rm "${CONTAINER_NAME}" || true
    fi
    
    # Run new container
    docker run -d \
        --name "${CONTAINER_NAME}" \
        -p "${HOST_PORT}:${CONTAINER_PORT}" \
        -e ACTIVE_PROFILE=docker \
        -v "$(pwd)/config:/config:ro" \
        "${IMAGE_NAME}:${IMAGE_TAG}"
    
    print_status "Container started successfully!"
    print_status "Application is available at: http://localhost:${HOST_PORT}"
    print_status "Health check: http://localhost:${HOST_PORT}/live"
    print_status "Metrics: http://localhost:${HOST_PORT}/metrics"
}

# Show container logs
show_logs() {
    print_status "Showing container logs:"
    docker logs -f "${CONTAINER_NAME}"
}

# Stop container
stop_container() {
    print_status "Stopping container: ${CONTAINER_NAME}"
    docker stop "${CONTAINER_NAME}" || true
    print_status "Container stopped!"
}

# Main script logic
case "${1:-}" in
    build)
        build_image
        ;;
    run)
        run_container
        ;;
    logs)
        show_logs
        ;;
    stop)
        stop_container
        ;;
    restart)
        stop_container
        build_image
        run_container
        ;;
    *)
        echo "Usage: $0 {build|run|logs|stop|restart}"
        echo ""
        echo "Commands:"
        echo "  build    - Build the Docker image"
        echo "  run      - Run the container"
        echo "  logs     - Show container logs"
        echo "  stop     - Stop the container"
        echo "  restart  - Stop, rebuild, and run the container"
        exit 1
        ;;
esac 