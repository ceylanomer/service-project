package circuitbreaker

import (
	"github.com/sony/gobreaker"
	"go.uber.org/zap"
	"time"
)

type CircuitBreakerConfig struct {
	Name                    string        `yaml:"name"`
	MaxRequests             uint32        `yaml:"maxRequests"`
	Interval                time.Duration `yaml:"interval"`
	Timeout                 time.Duration `yaml:"timeout"`
	RequestsVolumeThreshold uint32        `yaml:"requestsVolumeThreshold"`
	FailureThreshold        float64       `yaml:"failureThreshold"`
}

func NewCircuitBreakerConfig(config CircuitBreakerConfig) *gobreaker.CircuitBreaker {
	return gobreaker.NewCircuitBreaker(gobreaker.Settings{
		Name:        config.Name,
		MaxRequests: config.MaxRequests,
		Interval:    config.Interval,
		Timeout:     config.Timeout,
		ReadyToTrip: func(counts gobreaker.Counts) bool {
			failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
			return counts.Requests >= config.RequestsVolumeThreshold && failureRatio >= config.FailureThreshold
		},
		OnStateChange: func(name string, from gobreaker.State, to gobreaker.State) {
			zap.L().Info("Circuit breaker state changed", zap.String("name", name), zap.String("from", from.String()), zap.String("to", to.String()))
		},
	})
}

func NewCircuitBreakerWithName(config CircuitBreakerConfig, name string) *gobreaker.CircuitBreaker {
	return gobreaker.NewCircuitBreaker(gobreaker.Settings{
		Name:        name,
		MaxRequests: config.MaxRequests,
		Interval:    config.Interval,
		Timeout:     config.Timeout,
		ReadyToTrip: func(counts gobreaker.Counts) bool {
			failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
			return counts.Requests >= config.RequestsVolumeThreshold && failureRatio >= config.FailureThreshold
		},
		OnStateChange: func(name string, from gobreaker.State, to gobreaker.State) {
			zap.L().Info("Circuit breaker state changed", zap.String("name", name), zap.String("from", from.String()), zap.String("to", to.String()))
		},
	})
}
