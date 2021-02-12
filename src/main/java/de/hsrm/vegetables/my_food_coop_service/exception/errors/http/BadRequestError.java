package de.hsrm.vegetables.my_food_coop_service.exception.errors.http;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;

public class BadRequestError extends BaseError {

    public BadRequestError(String message, ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
