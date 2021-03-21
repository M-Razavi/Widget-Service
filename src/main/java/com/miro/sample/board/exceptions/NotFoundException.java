package com.miro.sample.board.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Widget not found")
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 3585441215024739391L;

}
