package de.hsrm.vegetables.service.exception.errors.security;

import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;

public class ExpiredTokenException extends UnauthorizedError {

    public ExpiredTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
