package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.StockApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.mapper.StockMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<AllStockResponse> stockGet(DeleteFilter deleted, List<StockStatus> filterByStatus) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // make sure the list exists
        if (filterByStatus == null) {
            filterByStatus = Collections.emptyList();
        }

        // A member cannot filter by ORDERED or OUTOFSTOCK
        if (!userPrincipal.getRoles()
                .contains(Role.ORDERER)) {
            if (filterByStatus.contains(StockStatus.ORDERED)) {
                throw new BadRequestError("A user without role ORDERER cannot use filter ORDERED", ErrorCode.MEMBER_CANNOT_USE_THIS_FILTER);
            }

            if (filterByStatus.contains(StockStatus.OUTOFSTOCK)) {
                throw new BadRequestError("A user without role ORDERER cannot use filter OUTOFSTOCK", ErrorCode.MEMBER_CANNOT_USE_THIS_FILTER);
            }
        }

        // Do not show stock in status ORDERED or OUTOFSTOCK to non Orderers
        ArrayList<StockStatus> allFilters = new ArrayList<>();
        if (!userPrincipal.getRoles()
                .contains(Role.ORDERER)) {
            if (!filterByStatus.contains(StockStatus.INSTOCK)) {
                allFilters.add(StockStatus.INSTOCK);
            }
            if (!filterByStatus.contains(StockStatus.ORDERED)) {
                allFilters.add(StockStatus.SPOILSSOON);
            }
        }

        allFilters.addAll(filterByStatus);

        List<StockResponse> items = StockMapper.listStockDtoToListStockResponse(stockService.getStock(deleted, allFilters));

        AllStockResponse response = new AllStockResponse();
        response.setItems(items);
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
                stockPatchRequest.getStockStatus()
        );

        StockResponse response = StockMapper.stockDtoToStockResponse(updatedStock);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ORDERER')")
    public ResponseEntity<StockResponse> stockPost(StockPostRequest stockPostRequest) {
        StockDto stockDto = stockService.addStock(stockPostRequest.getName(),
                stockPostRequest.getUnitType(),
                stockPostRequest.getQuantity(),
                stockPostRequest.getPricePerUnit(),
                stockPostRequest.getDescription(),
                stockPostRequest.getStockStatus()
        );
        StockResponse stockResponse = StockMapper.stockDtoToStockResponse(stockDto);
        return new ResponseEntity<>(stockResponse, HttpStatus.CREATED);
    }
}