package de.hsrm.vegetables.my_food_coop_service.exception.errors.http;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;

import java.io.Serial;

public class UnauthorizedError extends BaseError {

    @Serial
    private static final long serialVersionUID = -5589987286818510793L;

    public UnauthorizedError(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
