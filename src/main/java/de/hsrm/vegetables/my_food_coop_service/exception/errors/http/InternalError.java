package de.hsrm.vegetables.my_food_coop_service.exception.errors.http;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;

import java.io.Serial;

public class InternalError extends BaseError {

    @Serial
    private static final long serialVersionUID = -8890055491945859074L;

    public InternalError(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
