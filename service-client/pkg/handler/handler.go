package handler

import (
	"context"
	"errors"
	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v2"
	"go.uber.org/zap"
	"service-client/pkg/log"
)

type Request any
type Response any

var validate = validator.New()

type HandlerInterface[R Request, Res Response] interface {
	Handle(ctx context.Context, req *R) (*Res, error)
}

func Handle[R Request, Res Response](handler HandlerInterface[R, Res], statusCode ...int) fiber.Handler {
	return func(c *fiber.Ctx) error {
		var req R
		ctx := c.UserContext()

		if len(c.Body()) != 0 {
			if err := c.BodyParser(&req); err != nil && !errors.Is(err, fiber.ErrUnprocessableEntity) {
				zap.L().Error("Failed to parse request body", log.ExtractTraceInfoWithError(ctx, err)...)
				return fiber.NewError(fiber.StatusBadRequest, "Failed to parse request body")
			}
		}

		if err := c.ParamsParser(&req); err != nil {
			zap.L().Error("Failed to parse request parameters", log.ExtractTraceInfoWithError(ctx, err)...)
			return fiber.NewError(fiber.StatusBadRequest, "Failed to parse request parameters")
		}

		if err := c.QueryParser(&req); err != nil {
			zap.L().Error("Failed to parse request query", log.ExtractTraceInfoWithError(ctx, err)...)
			return fiber.NewError(fiber.StatusBadRequest, "Failed to parse request query")
		}

		if err := c.ReqHeaderParser(&req); err != nil {
			zap.L().Error("Failed to parse request headers", log.ExtractTraceInfoWithError(ctx, err)...)
			return fiber.NewError(fiber.StatusBadRequest, "Failed to parse request headers")
		}

		if err := validate.Struct(req); err != nil {
			zap.L().Error("Validation failed", log.ExtractTraceInfoWithError(ctx, err)...)
			return fiber.NewError(fiber.StatusBadRequest, "Validation failed")
		}

		res, err := handler.Handle(ctx, &req)
		if err != nil {
			zap.L().Error("Failed to handle request", log.ExtractTraceInfoWithError(ctx, err)...)
			return err
		}

		if len(statusCode) > 0 {
			if res == nil {
				return c.SendStatus(statusCode[0])
			} else {
				return c.Status(statusCode[0]).JSON(res)
			}
		}

		return c.JSON(res)
	}
}
