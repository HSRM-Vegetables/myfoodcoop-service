package de.hsrm.vegetables.my_food_coop_service.controller;

import de.hsrm.vegetables.my_food_coop_service.Util;
import de.hsrm.vegetables.my_food_coop_service.api.StockApi;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.DisposedDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.my_food_coop_service.mapper.StockMapper;
import de.hsrm.vegetables.my_food_coop_service.model.*;
import de.hsrm.vegetables.my_food_coop_service.security.UserPrincipal;
import de.hsrm.vegetables.my_food_coop_service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StockController implements StockApi {

    @NonNull
    private final StockService stockService;

    @Override
    @PreAuthorize("hasRole('ORDERER')")
    public ResponseEntity<Void> stockDelete(String itemId) {
        stockService.delete(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<AllStockResponse> stockGet(
            DeleteFilter deleted, List<StockStatus> filterByStatus, String sortBy, String sortOrder, Integer offset, Integer limit) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // make sure the list exists
        if (filterByStatus == null) {
            filterByStatus = Collections.emptyList();
        }

        // A member cannot filter by OUTOFSTOCK
        if (!userPrincipal.getRoles()
                .contains(Role.ORDERER) && filterByStatus.contains(StockStatus.OUTOFSTOCK)) {
            throw new BadRequestError("A user without role ORDERER cannot use filter OUTOFSTOCK", ErrorCode.MEMBER_CANNOT_USE_THIS_FILTER);
        }

        // Do not show stock in status OUTOFSTOCK to non Orderers
        ArrayList<StockStatus> allFilters = new ArrayList<>();
        if (!userPrincipal.getRoles()
                .contains(Role.ORDERER)) {
            // Add filter INSTOCK if necessary
            if (!filterByStatus.contains(StockStatus.INSTOCK)) {
                allFilters.add(StockStatus.INSTOCK);
            }
            // Add filter SPOILSSOON if necessary
            if (!filterByStatus.contains(StockStatus.SPOILSSOON)) {
                allFilters.add(StockStatus.SPOILSSOON);
            }
            // Add filter ORDERED if necessary
            if (!filterByStatus.contains(StockStatus.ORDERED)) {
                allFilters.add(StockStatus.ORDERED);
            }
        }

        allFilters.addAll(filterByStatus);

        // Query stock items from DB and create response

        Page<StockDto> page = stockService.getStock(deleted, allFilters, sortBy, sortOrder, offset, limit);

        List<StockResponse> items = StockMapper.listStockDtoToListStockResponse(page.getContent());

        AllStockResponse response = new AllStockResponse();
        response.setItems(items);

        if (page.getPageable()
                .isPaged()) {
            Pagination pagination = Util.createPagination(offset, limit, page.getTotalElements());
            response.setPagination(pagination);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<StockResponse> stockItemGet(String itemId) {
        StockDto stockDto = stockService.getById(itemId);
        StockResponse response = StockMapper.stockDtoToStockResponse(stockDto);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ORDERER')")
    public ResponseEntity<StockResponse> stockPatch(String itemId, StockPatchRequest stockPatchRequest) {
        StockDto updatedStock = stockService.update(
                itemId,
                stockPatchRequest.getName(),
                stockPatchRequest.getUnitType(),
                stockPatchRequest.getQuantity(),
                stockPatchRequest.getPricePerUnit(),
                stockPatchRequest.getDescription(),
                stockPatchRequest.getSustainablyProduced(),
                stockPatchRequest.getCertificates(),
                stockPatchRequest.getOriginCategory(),
                stockPatchRequest.getProducer(),
                stockPatchRequest.getSupplier(),
                stockPatchRequest.getOrderDate(),
                stockPatchRequest.getDeliveryDate(),
                stockPatchRequest.getStockStatus(),
                stockPatchRequest.getVat()
        );

        StockResponse response = StockMapper.stockDtoToStockResponse(updatedStock);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ORDERER')")
    public ResponseEntity<StockResponse> stockPost(StockPostRequest stockPostRequest) {
        StockDto stockDto = stockService.addStock(
                stockPostRequest.getName(),
                stockPostRequest.getUnitType(),
                stockPostRequest.getQuantity(),
                stockPostRequest.getPricePerUnit(),
                stockPostRequest.getDescription(),
                stockPostRequest.getSustainablyProduced(),
                stockPostRequest.getCertificates(),
                stockPostRequest.getOriginCategory(),
                stockPostRequest.getProducer(),
                stockPostRequest.getSupplier(),
                stockPostRequest.getOrderDate(),
                stockPostRequest.getDeliveryDate(),
                stockPostRequest.getStockStatus(),
                stockPostRequest.getVat()
        );
        StockResponse stockResponse = StockMapper.stockDtoToStockResponse(stockDto);
        return new ResponseEntity<>(stockResponse, HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<DisposedItem> dispose(String itemId, DisposeRequest disposeRequest) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // No need to query database for the user, we only need a DTO with the id
        // As we use the id from the UserPrincipal, which was checked for validity and if the user is deleted
        UserDto userDto = new UserDto();
        userDto.setId(userPrincipal.getId());

        DisposedDto disposedDto = stockService.dispose(itemId, userDto, disposeRequest.getAmount());

        return ResponseEntity.ok(StockMapper.disposedDtoToDisposedItem(disposedDto));
    }
}