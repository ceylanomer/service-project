package main

import (
	"context"
	"fmt"
	"github.com/gofiber/contrib/fiberzap/v2"
	"github.com/gofiber/contrib/otelfiber/v2"
	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/adaptor"
	"github.com/gofiber/fiber/v2/middleware/recover"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"go.uber.org/zap"
	"os"
	"os/signal"
	"service-client/app/clients"
	"service-client/app/handlers"
	"service-client/pkg/config"
	"service-client/pkg/handler"
	"service-client/pkg/log"
	"service-client/pkg/trace"
	"syscall"
	"time"
)

func main() {
	cfg := config.LoadConfig()

	logger := log.GetLogger()
	defer zap.L().Sync()

	tp := trace.InitTracer(cfg.DynamicConfigs)
	tracer := tp.Tracer("service-client")

	transport := clients.NewTransport()

	serviceApiClient := clients.NewServiceApiClient(transport, cfg)
	createServiceHandler := handlers.NewCreateServicesHandler(serviceApiClient, tracer)

	fiberApp := fiber.New(fiber.Config{
		IdleTimeout:    5 * time.Second,
		ReadTimeout:    3 * time.Second,
		WriteTimeout:   3 * time.Second,
		Concurrency:    256 * 1024,
		ReadBufferSize: 10240,
	})

	fiberApp.Use(recover.New())
	fiberApp.Use(otelfiber.Middleware())
	fiberApp.Use(
		fiberzap.New(
			fiberzap.Config{
				Logger:   logger,
				SkipURIs: []string{"/metrics", "/live", "/ready"},
			}),
	)

	fiberApp.Get("/metrics", adaptor.HTTPHandler(promhttp.Handler()))
	fiberApp.Get("/live", func(c *fiber.Ctx) error {
		return c.SendStatus(fiber.StatusOK)
	})
	fiberApp.Get("/ready", func(c *fiber.Ctx) error {
		return c.SendStatus(fiber.StatusOK)
	})

	api := fiberApp.Group("/api")
	v1 := api.Group("/v1")
	v1.Post("/services", handler.Handle(createServiceHandler, fiber.StatusCreated))

	go func() {
		serverAddr := fmt.Sprintf(":%d", cfg.AppConfig.Server.Port)
		if err := fiberApp.Listen(serverAddr); err != nil {
			logger.Fatal("Failed to start server", zap.Error(err))
		}
	}()

	logger.Info("Server started", zap.String("address", fmt.Sprintf("http://localhost:%d", cfg.AppConfig.Server.Port)))

	gracefulShutdown(fiberApp)
}

func gracefulShutdown(app *fiber.App) {
	shutdownChan := make(chan os.Signal, 1)
	signal.Notify(shutdownChan, syscall.SIGINT, syscall.SIGTERM)

	<-shutdownChan
	zap.L().Info("Shutting down...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := app.ShutdownWithContext(ctx); err != nil {
		zap.L().Error("Error during shutdown", zap.Error(err))
	} else {
		zap.L().Info("Server gracefully stopped")
	}
}
