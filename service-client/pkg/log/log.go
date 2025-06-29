package log

import (
	"context"
	"go.opentelemetry.io/otel/trace"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"os"
)

var logger *zap.Logger

func init() {
	encoderConfig := zap.NewProductionEncoderConfig()
	encoderConfig.TimeKey = "timestamp"
	encoderConfig.EncodeTime = zapcore.ISO8601TimeEncoder

	config := zap.Config{
		Level:             zap.NewAtomicLevelAt(zap.InfoLevel),
		Development:       false,
		DisableCaller:     false,
		DisableStacktrace: false,
		Sampling:          nil,
		Encoding:          "json",
		EncoderConfig:     encoderConfig,
		OutputPaths:       []string{"stderr"},
		ErrorOutputPaths:  []string{"stderr"},
		InitialFields: map[string]interface{}{
			"pid": os.Getpid(),
		},
	}

	logger = zap.Must(config.Build())

	zap.ReplaceGlobals(logger)
}

// GetLogger returns the global logger instance
func GetLogger() *zap.Logger {
	return logger
}

func ExtractTraceInfo(ctx context.Context, fields ...zap.Field) []zap.Field {
	span := trace.SpanFromContext(ctx)
	if !span.SpanContext().IsValid() {
		return nil
	}
	traceFields := []zap.Field{
		zap.String("trace_id", span.SpanContext().TraceID().String()),
		zap.String("span_id", span.SpanContext().SpanID().String()),
	}
	return append(fields, traceFields...)
}

func ExtractTraceInfoWithError(ctx context.Context, err error, fields ...zap.Field) []zap.Field {
	span := trace.SpanFromContext(ctx)
	if !span.SpanContext().IsValid() {
		return nil
	}
	traceFields := []zap.Field{
		zap.String("trace_id", span.SpanContext().TraceID().String()),
		zap.String("span_id", span.SpanContext().SpanID().String()),
		zap.Error(err),
	}
	return append(fields, traceFields...)
}
