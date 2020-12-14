package de.hsrm.vegetables.service.exception.errors;

import de.hsrm.vegetables.service.exception.ErrorCode;

public class ExampleError extends BaseError {
    private static final long serialVersionUID = -5530124675047902454L;

    public ExampleError() {
        this.errorCode = ErrorCode.EXAMPLE_EXCEPTION;
        this.message = "A predefined message";
    }

    public ExampleError(String message) {
        this.errorCode = ErrorCode.EXAMPLE_EXCEPTION;
        this.message = message;
    }

}
