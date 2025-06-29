package clients

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"github.com/hashicorp/go-retryablehttp"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.uber.org/zap"
	"net/http"
	"service-client/app/handlers/requests"
	"service-client/app/handlers/response"
	"service-client/pkg/config"
	"time"
)

type ServiceApiClient interface {
	CreateServices(ctx context.Context, request *requests.CreateServiceRequest) (*response.CreateServiceResponse, error)
}

type serviceApiClient struct {
	*retryablehttp.Client
	*config.Configuration
}

func NewServiceApiClient(transport *http.Transport, cfg *config.Configuration) ServiceApiClient {
	client := retryablehttp.NewClient()
	client.Logger = zap.NewStdLog(zap.L())
	client.HTTPClient.Transport = otelhttp.NewTransport(transport)
	client.RetryMax = 3
	client.RetryWaitMin = 100 * time.Millisecond
	client.RetryWaitMax = 10 * time.Second
	client.Backoff = retryablehttp.LinearJitterBackoff

	client.CheckRetry = func(ctx context.Context, resp *http.Response, err error) (bool, error) {
		if ctx.Err() != nil {
			return false, ctx.Err()
		}
		return retryablehttp.DefaultRetryPolicy(ctx, resp, err)
	}

	return &serviceApiClient{
		Client:        client,
		Configuration: cfg,
	}
}

func (c *serviceApiClient) CreateServices(ctx context.Context, request *requests.CreateServiceRequest) (*response.CreateServiceResponse, error) {
	zap.L().Info("Creating services", zap.Any("request", request))

	body, err := json.Marshal(request)
	if err != nil {
		zap.L().Error("Failed to marshal request", zap.Error(err))
		return nil, err
	}

	req, err := http.NewRequestWithContext(ctx,
		http.MethodPost,
		c.Configuration.AppConfig.ServiceApiClient.Host+"/api/services",
		bytes.NewReader(body))

	if err != nil {
		zap.L().Error("Failed to create new request", zap.Error(err))
		return nil, err
	}

	req.Header.Set("Content-Type", "application/json")

	retryableRequest, err := retryablehttp.FromRequest(req)
	if err != nil {
		zap.L().Error("Failed to create retryable request", zap.Error(err))
		return nil, err
	}

	resp, err := c.Client.Do(retryableRequest)
	if err != nil {
		zap.L().Error("Failed to send request", zap.Error(err))
		return nil, err
	}

	defer resp.Body.Close()

	if resp.StatusCode == http.StatusCreated || resp.StatusCode == http.StatusOK {
		var createServiceResponse response.CreateServiceResponse
		if err := json.NewDecoder(resp.Body).Decode(&createServiceResponse); err != nil {
			zap.L().Error("Failed to decode response", zap.Error(err))
			return nil, err
		}
		zap.L().Info("Services created successfully", zap.Any("response", createServiceResponse))
		return &createServiceResponse, nil
	}

	zap.L().Error("Unexpected response status", zap.Int("status", resp.StatusCode))
	return nil, fmt.Errorf("unexpected response status: %d", resp.StatusCode)

}
