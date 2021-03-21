package com.miro.sample.board.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

@Data
public class ApiError {

    /**
     * The constant ACCESS_DENIED.
     */
    public static final String ACCESS_DENIED = "Access denied!";
    /**
     * The constant INVALID_REQUEST.
     */
    public static final String INVALID_REQUEST = "Invalid request";

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private int statusCode;
    private String statusMessage;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;
    private String path;
    @JsonIgnore
    private Exception exception;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    /**
     * Instantiates a new Api error.
     *
     * @param request   the request
     * @param status    the status
     * @param message   the message
     * @param errors    the errors
     * @param exception the exception
     */
    public ApiError(WebRequest request, HttpStatus status, String message, List<String> errors, Exception exception) {
        this();
        statusCode = status.value();
        statusMessage = status.getReasonPhrase();
        this.message = message;
        path = request.getDescription(false);
        this.errors = errors;
        this.exception = exception;
    }

}