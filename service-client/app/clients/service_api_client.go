package clients

import (
	"context"
	"github.com/hashicorp/go-retryablehttp"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.uber.org/zap"
	"net/http"
	"service-client/pkg/config"
	"time"
)

type ServiceApiClient interface {
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
