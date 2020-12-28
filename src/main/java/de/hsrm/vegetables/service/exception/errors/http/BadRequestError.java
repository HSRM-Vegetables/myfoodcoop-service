package de.hsrm.vegetables.service.exception.errors.http;

import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.BaseError;

public class BadRequestError extends BaseError {

    public BadRequestError(String message, ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
