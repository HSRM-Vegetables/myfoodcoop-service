package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.BalanceMapper;
import de.hsrm.vegetables.service.mapper.PurchaseMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.PurchaseService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

    @NonNull
    private final UserService userService;

    @NonNull
    private final PurchaseService purchaseService;

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        BalanceDto balanceDto = balanceService.getBalance("Test3");
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

        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId).getUsername());

        //
        // balanceHistoryItems from balance changes
        //

        List<BalanceHistoryItemDto> balanceHistoryItemDtos = balanceService.getBalanceHistoryItems(balanceDto, pageNumber, pageSize);

        for (var balanceHistoryItem : balanceHistoryItemDtos) {
            if (!balanceDto.getName().equals(balanceHistoryItem.getBalanceDto().getName())) {
                throw new UnauthorizedError("The associated name for that balance history item does not match Header X-Username",
                        ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
            }
        }

        List<BalanceHistoryItem> balanceHistoryItems1 = balanceHistoryItemDtos.stream()
                .map(BalanceMapper::balanceHistoryItemDtoToBalanceHistoryItem)
                .collect(Collectors.toList());

        //
        // balanceHistoryItems from purchases
        //

        /*List<PurchaseDto> purchaseDtos = purchaseService.findAllByBalanceDtoAndCreatedOnBetween(balanceDto, fromDateConverted, toDateConverted);

        for (var purchaseDto : purchaseDtos) {
            if (!balanceDto.getName().equals(purchaseDto.getBalanceDto().getName())) {
                throw new UnauthorizedError("The associated name for that balance history item does not match Header X-Username",
                        ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
            }
        }

        List<BalanceHistoryItem> balanceHistoryItems2 = purchaseDtos.stream()
                .map(PurchaseMapper::purchaseDtoToBalanceHistoryItems)
                .flatMap(List::stream)
                .collect(Collectors.toList());*/

        //
        // Create response
        //

        Pagination pagination = new Pagination();
        pagination.setPageNumber(pageNumber);
        pagination.setPageSize(pageSize);
        pagination.setTotal(balanceHistoryItemDtos.size());

       /* List<BalanceHistoryItem> balanceHistoryItems = Stream
                .concat(balanceHistoryItems1.stream(), balanceHistoryItems2.stream())
                .collect(Collectors.toList());*/

        System.out.println("###");
        System.out.println("###");
        System.out.println("###");
        System.out.println(balanceHistoryItems1);

        BalanceHistoryResponse balanceHistoryResponse = new BalanceHistoryResponse();
        balanceHistoryResponse.setBalanceHistoryItems(balanceHistoryItems1);
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

        userBalanceHistoryGet(userId, LocalDate.now().minusDays(3), LocalDate.now(), 0, 100);
        userBalanceHistoryGet(userId, LocalDate.now().minusDays(3), LocalDate.now(), 1, 2);

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
