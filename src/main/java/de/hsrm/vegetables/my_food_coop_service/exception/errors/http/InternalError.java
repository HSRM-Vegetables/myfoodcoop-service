package de.hsrm.vegetables.my_food_coop_service.exception.errors.http;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;

public class InternalError extends BaseError {

    public InternalError(String message, ErrorCode errorCode) {
        super (message, errorCode);
    }
}
