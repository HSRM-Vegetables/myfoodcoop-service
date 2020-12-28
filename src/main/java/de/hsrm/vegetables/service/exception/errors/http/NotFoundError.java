package de.hsrm.vegetables.service.exception.errors.http;

import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.BaseError;

public class NotFoundError extends BaseError {

    private static final long serialVersionUID = -5589987286818510793L;

    public NotFoundError(String message, ErrorCode errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

}
