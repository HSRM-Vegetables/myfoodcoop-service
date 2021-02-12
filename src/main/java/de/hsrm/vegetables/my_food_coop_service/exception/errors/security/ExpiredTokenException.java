package de.hsrm.vegetables.my_food_coop_service.exception.errors.security;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.UnauthorizedError;

public class ExpiredTokenException extends UnauthorizedError {

    public ExpiredTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
