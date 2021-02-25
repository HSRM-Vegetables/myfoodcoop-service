package de.hsrm.vegetables.my_food_coop_service.exception.errors;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;

public abstract class BaseError extends RuntimeException {

    private static final long serialVersionUID = 7568423597544365131L;

    protected final ErrorCode errorCode;

    protected final String message;

    protected BaseError(String message, ErrorCode errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
