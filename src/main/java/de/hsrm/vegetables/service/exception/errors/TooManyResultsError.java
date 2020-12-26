package de.hsrm.vegetables.service.exception.errors;

import de.hsrm.vegetables.service.exception.ErrorCode;

public class TooManyResultsError extends BaseError {

    public TooManyResultsError() {
        this.errorCode = ErrorCode.TOO_MANY_RESULTS;
        this.message = "Got too many results from Database, expected only one.";
    }

    public TooManyResultsError(String message) {
        this.errorCode = ErrorCode.TOO_MANY_RESULTS;
        this.message = message;
    }

}
