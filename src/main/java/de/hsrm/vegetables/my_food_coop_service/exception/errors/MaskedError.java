package de.hsrm.vegetables.my_food_coop_service.exception.errors;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;

public class MaskedError extends BaseError {

    public MaskedError(ErrorCode errorCode) {
        super("An internal server error occurred", errorCode);
    }

}
