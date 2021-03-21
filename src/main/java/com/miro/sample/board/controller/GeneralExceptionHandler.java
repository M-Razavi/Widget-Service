package com.miro.sample.board.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import com.miro.sample.board.exceptions.ApiError;
import com.miro.sample.board.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The type General exception handler.
 */
@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * The constant ERROR_MESSAGE_TEMPLATE.
     */
    public static final String ERROR_MESSAGE_TEMPLATE = "message: %s %n requested uri: %s";

    /**
     * The constant ERROR_CONCURRENT_MODIFICATION.
     */
    public static final String ERROR_CONCURRENT_MODIFICATION = "Concurrent Widget modification happened or tried to update an old version";

    /**
     * The constant FIELD_ERROR_SEPARATOR.
     */
    public static final String FIELD_ERROR_SEPARATOR = ": ";
    private static final String ERRORS_FOR_PATH = "errors {} for path {}. exception class {}";


    /**
     * Handle constraint violation response entity.
     *
     * @param exception the exception
     * @param request   the request
     * @return the response entity
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
        ConstraintViolationException exception, WebRequest request) {
        final List<String> validationErrors = exception.getConstraintViolations().stream()
            .map(violation ->
                violation.getPropertyPath() + FIELD_ERROR_SEPARATOR + violation.getMessage())
            .collect(Collectors.toList());

        String message = "Widget is invalid";
        return buildResponseEntity(new ApiError(request, HttpStatus.BAD_REQUEST, message,
            validationErrors, exception));
    }


    /**
     * Handle jpa optimistic locking failure exception response entity.
     *
     * @param exception the exception
     * @param request   the request
     * @return the response entity
     */
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Object> handleObjectOptimisticLockingFailureExceptionn(
        ObjectOptimisticLockingFailureException exception, WebRequest request) {
        return buildResponseEntity(new ApiError(request, HttpStatus.BAD_REQUEST, ERROR_CONCURRENT_MODIFICATION,
            null, exception));
    }

    /**
     * Handle not found exception response entity.
     *
     * @param exception the exception
     * @param request   the request
     * @return the response entity
     */
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(
        NotFoundException exception, WebRequest request) {
        String message = "Widget not found";
        return buildResponseEntity(new ApiError(request, HttpStatus.NOT_FOUND, message,
            null, exception));
    }

    /**
     * A general handler for all uncaught exceptions.
     *
     * @param exception the exception
     * @param request   the request
     * @return the response entity
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception exception, WebRequest request) {
        ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);
        final HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
        final String localizedMessage = exception.getLocalizedMessage();
        final String path = request.getDescription(false);
        String message = (StringUtils.isNotEmpty(localizedMessage) ? localizedMessage : status.getReasonPhrase());
        log.error(String.format(ERROR_MESSAGE_TEMPLATE, message, path), exception);

        return buildResponseEntity(new ApiError(request, HttpStatus.BAD_REQUEST, "Internal error occurred",
            null, exception));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<String> validationErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + FIELD_ERROR_SEPARATOR + error.getDefaultMessage())
            .collect(Collectors.toList());

        String message = "Widget field(s) is not valid";
        return buildResponseEntity(new ApiError(request, HttpStatus.BAD_REQUEST, message,
            validationErrors, exception));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        String message = "Malformed JSON request";
        return buildResponseEntity(new ApiError(request, HttpStatus.BAD_REQUEST, message,
            Collections.singletonList(exception.getLocalizedMessage()), exception));
    }

    /**
     * Build a detailed information about the exception in the response.
     *
     * @param apiError dto of exception
     * @return ResponseEntity
     */
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("Content-Type", List.of(MediaType.APPLICATION_JSON_VALUE));
        log.error(ERRORS_FOR_PATH, apiError.getErrors() == null ? "" : apiError.getErrors().toString(),
            apiError.getPath(), apiError.getException().getClass().getSimpleName());
        return new ResponseEntity<>(apiError, headers, apiError.getStatusCode());
    }
}