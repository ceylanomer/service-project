package handlers

import (
	"context"
	"go.opentelemetry.io/otel/trace"
	"service-client/app/clients"
	"service-client/app/handlers/requests"
	"service-client/app/handlers/response"
)

type CreateServicesHandler struct {
	serviceApiClient clients.ServiceApiClient
	tracer           trace.Tracer
}

func NewCreateServicesHandler(serviceApiClient clients.ServiceApiClient, tracer trace.Tracer) *CreateServicesHandler {
	return &CreateServicesHandler{
		serviceApiClient: serviceApiClient,
		tracer:           tracer,
	}
}

func (h *CreateServicesHandler) Handle(ctx context.Context, request *requests.CreateServiceRequest) (*response.CreateServiceResponse, error) {
	var span trace.Span
	if h.tracer != nil {
		ctx, span = h.tracer.Start(ctx, "CreateServicesHandler.Handle")
		defer span.End()
	}
	resp, err := h.serviceApiClient.CreateServices(ctx, request)
	if err != nil {
		if span != nil {
			span.RecordError(err)
		}
		return nil, err
	}
	return resp, nil
}
