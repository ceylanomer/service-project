# Build stage
FROM golang:1.24.0-alpine AS builder

# Install necessary packages for building
RUN apk add --no-cache git ca-certificates tzdata

# Set working directory
WORKDIR /app

# Copy go mod files first for better caching
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download && go mod verify

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build \
    -ldflags='-w -s -extldflags "-static"' \
    -a -installsuffix cgo \
    -o service-client .

# Final stage
FROM scratch

# Copy CA certificates from builder stage
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/

# Copy timezone data
COPY --from=builder /usr/share/zoneinfo /usr/share/zoneinfo

# Copy the binary
COPY --from=builder /app/service-client /service-client

# Copy configuration files
COPY --from=builder /app/config /config

# Create a non-root user (using numeric ID for scratch image)
USER 65534:65534

# Expose the port (default from config is 8081)
EXPOSE 8081

# Health check using the /live endpoint
# Note: Since we're using scratch image, we'll rely on the application's built-in health endpoints
# External health checks can be done via HTTP calls to :8081/live

# Run the application
ENTRYPOINT ["/service-client"]
