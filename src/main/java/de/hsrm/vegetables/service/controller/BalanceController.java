package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.BalanceMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

    @NonNull
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId)
                .getUsername());
        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceHistoryResponse> userBalanceHistoryGet(
            String userId, LocalDate fromDate, LocalDate toDate, Integer pageNumber, Integer pageSize) {

        LocalDate today = LocalDate.now();

        if (fromDate.isAfter(today) || toDate.isAfter(today)) {
            throw new BadRequestError("Report Date cannot be in the future", ErrorCode.REPORT_DATA_IN_FUTURE);
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestError("fromDate cannot be after toDate", ErrorCode.TO_DATE_AFTER_FROM_DATE);
        }

        OffsetDateTime fromDateConverted = OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDateConverted = OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC);

        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId)
                .getUsername());

        //
        // Query balance history items
        //

        Page<BalanceHistoryItemDto> balanceHistoryItemDtoPage = balanceService.findAllByBalanceDtoAndCreatedOnBetween(
                balanceDto, fromDateConverted, toDateConverted, PageRequest.of(pageNumber, pageSize));

        List<BalanceHistoryItem> balanceHistoryItems = balanceHistoryItemDtoPage.stream()
                .peek(balanceHistoryItemDto -> {
                    if (!balanceHistoryItemDto.getBalanceDto()
                            .getName()
                            .equals(balanceDto.getName())) {
                        throw new UnauthorizedError(
                                "The associated name for that balance history item does not match Header X-Username",
                                ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
                    }
                })
                .map(BalanceMapper::balanceHistoryItemDtoToBalanceHistoryItem)
                .collect(Collectors.toList());

        //
        // Create response
        //

        Pagination pagination = new Pagination();
        pagination.setPageNumber(pageNumber);
        pagination.setPageSize(pageSize);
        pagination.setTotalPages(balanceHistoryItemDtoPage.getTotalPages());
        pagination.setTotalElements(balanceHistoryItemDtoPage.getTotalElements());

        BalanceHistoryResponse balanceHistoryResponse = new BalanceHistoryResponse();
        balanceHistoryResponse.setBalanceHistoryItems(balanceHistoryItems);
        balanceHistoryResponse.setPagination(pagination);

        return ResponseEntity.ok(balanceHistoryResponse);
    }


    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balancePatch(String userId, BalancePatchRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.upsert(userPrincipal.getUsername(), request.getBalance());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceTopup(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.topup(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceWithdraw(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.withdraw(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
