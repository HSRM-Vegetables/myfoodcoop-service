package de.hsrm.vegetables.my_food_coop_service.exception.errors.http;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;

public class UnauthorizedError extends BaseError {

    private static final long serialVersionUID = -5589987286818510793L;

    public UnauthorizedError(String message, ErrorCode errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

}
