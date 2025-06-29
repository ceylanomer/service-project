#!/bin/bash

# Simple script to run the service project on local Linux machine
# Uses Docker Compose for easy setup

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}" >&2
}

warn() {
    echo -e "${YELLOW}[$(date +'%H:%M:%S')] $1${NC}"
}

# Check if docker and docker-compose are installed
check_requirements() {
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
}

# Function to show service status
show_status() {
    echo
    log "=== Service Status ==="
    docker-compose ps
    echo
    if docker-compose ps | grep -q "Up"; then
        log "Services are running:"
        echo "  🌐 Service API: http://localhost:8080"
        echo "  🌐 Service Client: http://localhost:8081"  
        echo "  🌐 Bulk Retrieve: http://localhost:8081/api/v1/services/batch"
        echo "  🗄️  MongoDB: localhost:27017"
    fi
}

# Function to test the services
test_services() {
    log "Testing services..."
    echo
    
    # Test Service API health
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        log "✅ Service API is healthy"
    else
        warn "❌ Service API health check failed"
    fi
    
    # Test Service Client health  
    if curl -s http://localhost:8081/ready > /dev/null; then
        log "✅ Service Client is healthy"
    else
        warn "❌ Service Client health check failed"
    fi
    
    # Test bulk retrieve endpoint
    echo
    log "Testing bulk retrieve endpoint..."
    curl -X GET http://localhost:8081/api/v1/services/batch \
         -H "Content-Type: application/json" \
         -d '{"serviceIds": ["test-1", "test-2"]}' \
         -w "\nHTTP Status: %{http_code}\n" || true
}

# Main function
main() {
    case "${1:-start}" in
        "start")
            check_requirements
            log "Starting all services with Docker Compose..."
            docker-compose up -d
            echo
            log "Waiting for services to be ready..."
            sleep 10
            show_status
            ;;
        "stop")
            log "Stopping all services..."
            docker-compose down
            log "All services stopped"
            ;;
        "restart")
            log "Restarting all services..."
            docker-compose restart
            sleep 5
            show_status
            ;;
        "logs")
            docker-compose logs -f
            ;;
        "status")
            show_status
            ;;
        "test")
            test_services
            ;;
        "clean")
            log "Stopping and removing all containers, networks, and volumes..."
            docker-compose down -v --remove-orphans
            log "Cleanup completed"
            ;;
        *)
            echo "Usage: $0 {start|stop|restart|logs|status|test|clean}"
            echo
            echo "Commands:"
            echo "  start   - Start all services (default)"
            echo "  stop    - Stop all services"
            echo "  restart - Restart all services"
            echo "  logs    - Show service logs (follow mode)"
            echo "  status  - Show service status"
            echo "  test    - Test service endpoints"
            echo "  clean   - Stop and remove everything"
            echo
            echo "Requirements:"
            echo "  - Docker"
            echo "  - Docker Compose"
            exit 1
            ;;
    esac
}

main "$@" 