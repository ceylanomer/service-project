package retryer

import (
	"context"
	"github.com/avast/retry-go/v4"
	"time"
)

func Do(ctx context.Context, retryableFunc retry.RetryableFunc, onRetry retry.OnRetryFunc, retryIfFunc retry.RetryIfFunc) error {
	return retry.Do(
		retryableFunc,
		retry.RetryIf(retryIfFunc),
		retry.Attempts(3),
		retry.LastErrorOnly(true),
		retry.OnRetry(onRetry),
		retry.Context(ctx),
		retry.Delay(10*time.Millisecond),
		retry.MaxJitter(3*time.Millisecond),
		retry.DelayType(retry.CombineDelay(retry.FixedDelay, retry.RandomDelay)),
	)
}
