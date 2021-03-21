package com.miro.sample.board.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Concurrent Widget modification happened.")
public class ConcurrentWidgetModificationException extends RuntimeException {
    public ConcurrentWidgetModificationException(String message) {
        super(message);
    }
}
