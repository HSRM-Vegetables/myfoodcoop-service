package de.hsrm.vegetables.my_food_coop_service;

import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.my_food_coop_service.model.Pagination;

import java.time.LocalDate;

public class Util {

    private Util() {
        // hide implicit public constructor
    }

    public static Pagination createPagination(Integer offset, Integer limit, Long total) {
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setTotal(total);

        return pagination;
    }

    /**
     * Check that fromDate <= toDate <= today
     */
    public static void checkDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();

        if (fromDate.isAfter(today) || toDate.isAfter(today)) {
            throw new BadRequestError("Report Date cannot be in the future", ErrorCode.REPORT_DATA_IN_FUTURE);
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestError("fromDate cannot be after toDate", ErrorCode.TO_DATE_AFTER_FROM_DATE);
        }
    }
}
