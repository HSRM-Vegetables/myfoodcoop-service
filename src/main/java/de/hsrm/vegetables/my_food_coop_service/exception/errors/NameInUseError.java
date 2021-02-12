package de.hsrm.vegetables.my_food_coop_service.exception.errors;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;

public class NameInUseError extends BaseError {

    public NameInUseError() {
        this.errorCode = ErrorCode.TOO_MANY_RESULTS;
        this.message = "Got too many results from Database, expected only one.";
    }

}
