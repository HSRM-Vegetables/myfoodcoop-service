package de.hsrm.vegetables.service.exception.errors.http;

import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.BaseError;

public class InternalError extends BaseError {

    public InternalError(String message, ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
