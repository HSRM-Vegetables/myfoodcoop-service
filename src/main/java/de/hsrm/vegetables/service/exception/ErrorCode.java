package de.hsrm.vegetables.service.exception;

public enum ErrorCode {

    // 400
    MISSING_SERVLET_REQUEST_PARAMETER(400001),
    SERVLET_REQUEST_BINDING_EXCEPTION(400002),
    TYPE_MISMATCH(400003),
    MESSAGE_NOT_READABLE(400004),
    METHOD_ARGUMENT_NOT_VALID(400005),
    MISSING_SERVLET_REQUEST_PART(400006),
    BIND_EXCEPTION(400007),
    NO_FRACTIONAL_QUANTITY(400008),
    ITEM_IS_DELETED(400009),
    MULTIPLE_ITEMS_WITH_SAME_ID(400010),
    CANNOT_PURCHASE_DELETED_ITEM(400011),
    TO_DATE_AFTER_FROM_DATE(400012),
    REPORT_DATA_IN_FUTURE(400013),

    // 401
    USERNAME_DOES_NOT_MATCH_PURCHASE(401001),

    // 404
    NO_HANDLER_FOUND(404001),
    NO_BALANCE_FOUND(404002),
    NO_STOCK_ITEM_FOUND(404003),
    NO_PURCHASE_FOUND(404004),

    // 405
    METHOD_NOT_ALLOWED(405001),

    // 406
    NOT_ACCEPTABLE(406001),

    // 415
    UNSUPPORTED_MEDIA_TYPE(415001),

    // 500
    INTERNAL_EXCEPTION(500001),
    MISSING_PATH_VARIABLE(500002),
    CONVERSION_NOT_SUPPORTED(500003),
    MESSAGE_NOT_WRITABLE(500004),
    EXAMPLE_EXCEPTION(500005),
    TOO_MANY_RESULTS(500006),
    NAME_IN_USE(500007),
    STOCK_DTO_NOT_FOUND(500008),

    // 503
    ASYNC_REQUEST_TIMEOUT(503001);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
