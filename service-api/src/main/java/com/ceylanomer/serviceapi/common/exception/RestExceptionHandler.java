package com.ceylanomer.serviceapi.common.exception;

import com.ceylanomer.serviceapi.common.controller.BaseController;
import com.ceylanomer.serviceapi.common.response.ErrorResponse;
import com.ceylanomer.serviceapi.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends BaseController {
    private final MessageSource messageSource;

    public RestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<ErrorResponse> handleException(Exception exception, Locale locale) {
        log.error("An error occurred! Details: ", exception);
        return createErrorResponseFromMessageSource("common.system.error.occurred", locale);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Response<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException methodNotSupportedException, Locale locale) {
        log.error("HttpRequestMethodNotSupportedException occurred: ", methodNotSupportedException);
        return createErrorResponseFromMessageSource("common.client.methodNotSupported", locale);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<ErrorResponse> handleMessageNotReadableException(HttpMessageNotReadableException messageNotReadableException, Locale locale) {
        log.error("Bad Request!", messageNotReadableException);
        return createErrorResponseFromMessageSource("common.client.badRequest", locale);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<ErrorResponse> handleRequestPropertyBindingException(WebExchangeBindException webExchangeBindException, Locale locale) {
        log.error("Bad Request!", webExchangeBindException);
        return createFieldErrorResponse(webExchangeBindException.getBindingResult(), locale);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<ErrorResponse> handleRequestPropertyBindingException(BindException bindException, Locale locale) {
        log.error("Bad Request!", bindException);
        return createFieldErrorResponse(bindException.getBindingResult(), locale);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<ErrorResponse> handleInvalidArgumentException(MethodArgumentNotValidException methodArgumentNotValidException, Locale locale) {
        log.error("Bad Request!", methodArgumentNotValidException);
        return createFieldErrorResponse(methodArgumentNotValidException.getBindingResult(), locale);
    }

    @ExceptionHandler(ServiceApiBusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response<ErrorResponse> handleProjectSettingsApiBusinessException(ServiceApiBusinessException serviceApiBusinessException, Locale locale) {
        log.error("Business exception occurred!", serviceApiBusinessException);
        return createErrorResponseFromMessageSource(serviceApiBusinessException.getKey(), locale, serviceApiBusinessException.getArgs());
    }

    @ExceptionHandler(ServiceApiDataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<ErrorResponse> handleProjectSettingsApiDataNotFoundException(ServiceApiDataNotFoundException serviceApiDataNotFoundException, Locale locale) {
        log.error("Data not found exception is occurred", serviceApiDataNotFoundException);
        return createErrorResponseFromMessageSource(serviceApiDataNotFoundException.getKey(), locale, serviceApiDataNotFoundException.getArgs());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<ErrorResponse> handleNoSuchElementException(NoSuchElementException noSuchElementException, Locale locale) {
        log.error("NoSuchElementException exception is occurred", noSuchElementException);
        return createErrorResponseFromMessageSource("common.client.noSuchElement", locale);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<ErrorResponse> handleEmptyResultDataAccessException(EmptyResultDataAccessException emptyResultDataAccessException, Locale locale) {
        log.error("EmptyResultDataAccessException exception is occurred", emptyResultDataAccessException);
        return createErrorResponseFromMessageSource("common.client.EmptyResultDataAccess", locale);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException, Locale locale) {
        log.error("MethodArgumentTypeMismatchException occurred!", methodArgumentTypeMismatchException);
        return createErrorResponseFromMessageSource("common.client.typeMismatch", locale, methodArgumentTypeMismatchException.getName());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<ErrorResponse> handleAccessDeniedException(AccessDeniedException accessDeniedException, Locale locale) {
        log.error("AccessDeniedException occurred!", accessDeniedException);
        return createErrorResponseFromMessageSource("common.client.unauthorized", locale);
    }

    private Response<ErrorResponse> createFieldErrorResponse(BindingResult bindingResult, Locale locale) {
        FieldError fieldError = bindingResult.getFieldErrors().stream()
                .filter(error -> Objects.nonNull(error.getDefaultMessage()))
                .findFirst()
                .orElse(new FieldError(bindingResult.getFieldErrors().get(0).getObjectName(),
                        bindingResult.getFieldErrors().get(0).getField(), "common.client.requiredField"));

        List<String> requiredFieldErrorMessages = retrieveLocalizationMessage(fieldError.getDefaultMessage(), locale, fieldError.getField());
        String code = requiredFieldErrorMessages.get(0);
        String errorMessage = requiredFieldErrorMessages.get(1);
        log.debug("Exception occurred while request validation: {}", errorMessage);
        return respond(new ErrorResponse(code, errorMessage));
    }

    private Response<ErrorResponse> createErrorResponseFromMessageSource(String key, Locale locale, String... args) {
        List<String> messageList = retrieveLocalizationMessage(key, locale, args);
        return respond(new ErrorResponse(messageList.get(0), messageList.get(1)));
    }

    private List<String> retrieveLocalizationMessage(String key, Locale locale, String... args) {
        String message = messageSource.getMessage(key, args, locale);
        return Pattern.compile(";").splitAsStream(message).collect(Collectors.toList());
    }

}
