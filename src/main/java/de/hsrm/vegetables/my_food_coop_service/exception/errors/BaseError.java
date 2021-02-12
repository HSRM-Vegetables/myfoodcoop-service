package de.hsrm.vegetables.my_food_coop_service.exception.errors;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;

public abstract class BaseError extends RuntimeException {

    private static final long serialVersionUID = 7568423597544365131L;

    protected ErrorCode errorCode;

    protected String message;

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
