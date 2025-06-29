package handlers

import (
	"context"
	"go.opentelemetry.io/otel/trace"
	"service-client/app/clients"
	"service-client/app/handlers/requests"
	"service-client/app/handlers/response"
)

type CreateServiceHandler struct {
	serviceApiClient clients.ServiceApiClient
	tracer           trace.Tracer
}

func NewCreateServiceHandler(serviceApiClient clients.ServiceApiClient, tracer trace.Tracer) *CreateServiceHandler {
	return &CreateServiceHandler{
		serviceApiClient: serviceApiClient,
		tracer:           tracer,
	}
}

func (h *CreateServiceHandler) Handle(ctx context.Context, request *requests.CreateServiceRequest) (*response.ServiceResponse, error) {
	var span trace.Span
	if h.tracer != nil {
		ctx, span = h.tracer.Start(ctx, "CreateServiceHandler.Handle")
		defer span.End()
	}
	resp, err := h.serviceApiClient.CreateService(ctx, request)
	if err != nil {
		if span != nil {
			span.RecordError(err)
		}
		return nil, err
	}
	return resp, nil
}
