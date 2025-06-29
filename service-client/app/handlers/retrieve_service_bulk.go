package handlers

import (
	"context"
	"fmt"
	"service-client/app/clients"
	"service-client/app/handlers/requests"
	"service-client/app/handlers/response"
	"service-client/pkg/config"
	"service-client/pkg/log"
	"sync"
	"time"

	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
	"go.uber.org/zap"
)

type RetrieveServiceBulkHandler struct {
	serviceApiClient clients.ServiceApiClient
	tracer           trace.Tracer
	cfg              *config.Configuration
}

func NewRetrieveServiceBulkHandler(serviceApiClient clients.ServiceApiClient, tracer trace.Tracer, cfg *config.Configuration) *RetrieveServiceBulkHandler {
	return &RetrieveServiceBulkHandler{
		serviceApiClient: serviceApiClient,
		tracer:           tracer,
		cfg:              cfg,
	}
}

func (h *RetrieveServiceBulkHandler) Handle(ctx context.Context, request *requests.RetrieveServiceRequest) (*response.AnyResponse, error) {
	startTime := time.Now()

	var span trace.Span
	if h.tracer != nil {
		ctx, span = h.tracer.Start(ctx, "RetrieveServiceBulkHandler.Handle")
		defer span.End()
	}

	zap.L().Info("Handling bulk retrieve service request",
		log.ExtractTraceInfo(ctx)...)

	// Prepare service IDs for bulk processing
	serviceIds := h.prepareServiceIds(request)
	if len(serviceIds) == 0 {
		return &response.AnyResponse{
			Summary: &response.BulkSummary{
				TotalRequests:    0,
				SuccessfulCount:  0,
				FailedCount:      0,
				ProcessingTimeMs: time.Since(startTime).Milliseconds(),
			},
		}, nil
	}

	// Get configuration values
	parallelRequests := h.cfg.DynamicConfigs.NumberOfParallelRequests
	numberOfSteps := h.cfg.DynamicConfigs.NumberOfSteps

	if span != nil {
		span.SetAttributes(
			attribute.Int("total_service_ids", len(serviceIds)),
			attribute.Int("parallel_requests", parallelRequests),
			attribute.Int("number_of_steps", numberOfSteps),
		)
	}

	zap.L().Info("Starting bulk retrieve operation",
		zap.Int("totalServiceIds", len(serviceIds)),
		zap.Int("parallelRequests", parallelRequests),
		zap.Int("numberOfSteps", numberOfSteps),
	)

	// Process requests in steps
	allResults := make([]response.BulkResult, 0, len(serviceIds))
	stepSize := len(serviceIds) / numberOfSteps
	if stepSize == 0 {
		stepSize = 1
	}

	for step := 0; step < numberOfSteps && step*stepSize < len(serviceIds); step++ {
		start := step * stepSize
		end := start + stepSize
		if end > len(serviceIds) || step == numberOfSteps-1 {
			end = len(serviceIds)
		}

		stepServiceIds := serviceIds[start:end]
		stepResults := h.processStep(ctx, step+1, stepServiceIds, parallelRequests)
		allResults = append(allResults, stepResults...)

		zap.L().Info("Completed step",
			zap.Int("step", step+1),
			zap.Int("processedCount", len(stepServiceIds)),
			zap.Int("totalProcessed", len(allResults)),
		)
	}

	// Calculate summary
	successCount := 0
	failedCount := 0
	for _, result := range allResults {
		if result.Success {
			successCount++
		} else {
			failedCount++
		}
	}

	summary := &response.BulkSummary{
		TotalRequests:    len(serviceIds),
		SuccessfulCount:  successCount,
		FailedCount:      failedCount,
		ProcessingTimeMs: time.Since(startTime).Milliseconds(),
	}

	if span != nil {
		span.SetAttributes(
			attribute.Int("successful_requests", successCount),
			attribute.Int("failed_requests", failedCount),
			attribute.Int64("processing_time_ms", summary.ProcessingTimeMs),
		)
	}

	zap.L().Info("Bulk retrieve operation completed",
		zap.Int("totalRequests", summary.TotalRequests),
		zap.Int("successfulCount", summary.SuccessfulCount),
		zap.Int("failedCount", summary.FailedCount),
		zap.Int64("processingTimeMs", summary.ProcessingTimeMs),
	)

	return &response.AnyResponse{
		Summary: summary,
	}, nil
}

func (h *RetrieveServiceBulkHandler) prepareServiceIds(request *requests.RetrieveServiceRequest) []string {
	if len(request.ServiceIds) > 0 {
		return request.ServiceIds
	}
	if request.ServiceId != "" {
		return []string{request.ServiceId}
	}
	return []string{}
}

func (h *RetrieveServiceBulkHandler) processStep(ctx context.Context, stepNumber int, serviceIds []string, maxParallel int) []response.BulkResult {
	var stepSpan trace.Span
	if h.tracer != nil {
		ctx, stepSpan = h.tracer.Start(ctx, fmt.Sprintf("RetrieveServiceBulkHandler.ProcessStep_%d", stepNumber))
		defer stepSpan.End()
		stepSpan.SetAttributes(
			attribute.Int("step_number", stepNumber),
			attribute.Int("step_service_count", len(serviceIds)),
			attribute.Int("max_parallel", maxParallel),
		)
	}

	results := make([]response.BulkResult, len(serviceIds))
	semaphore := make(chan struct{}, maxParallel)
	var wg sync.WaitGroup

	for i, serviceId := range serviceIds {
		wg.Add(1)
		go func(index int, id string) {
			defer wg.Done()

			semaphore <- struct{}{}
			defer func() { <-semaphore }()

			result := h.processServiceRequest(ctx, id)
			results[index] = result
		}(i, serviceId)
	}

	wg.Wait()
	return results
}

func (h *RetrieveServiceBulkHandler) processServiceRequest(ctx context.Context, serviceId string) response.BulkResult {
	var reqSpan trace.Span
	if h.tracer != nil {
		ctx, reqSpan = h.tracer.Start(ctx, "RetrieveServiceBulkHandler.ProcessServiceRequest")
		defer reqSpan.End()
		reqSpan.SetAttributes(attribute.String("service_id", serviceId))
	}

	serviceResponse, err := h.serviceApiClient.RetrieveServiceById(ctx, serviceId)
	if err != nil {
		if reqSpan != nil {
			reqSpan.SetAttributes(attribute.Bool("success", false))
		}
		return response.BulkResult{
			ServiceId: serviceId,
			Success:   false,
			Error:     err.Error(),
		}
	}

	if reqSpan != nil {
		reqSpan.SetAttributes(attribute.Bool("success", true))
	}

	return response.BulkResult{
		ServiceId: serviceId,
		Success:   true,
		Data:      serviceResponse,
	}
}
