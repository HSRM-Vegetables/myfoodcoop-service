package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.StockApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.mapper.StockMapper;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StockController implements StockApi {

    @NonNull
    private final StockService stockService;

    @Override
    public ResponseEntity<Void> stockDelete(String itemId) {
        stockService.delete(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<AllStockResponse> stockGet(DeleteFilter deleted) {
        List<StockResponse> items = StockMapper.listStockDtoToListStockResponse(stockService.getAll(deleted));
        AllStockResponse response = new AllStockResponse();
        response.setItems(items);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockResponse> stockItemGet(String itemId) {
        StockDto stockDto = stockService.getById(itemId);
        StockResponse response = StockMapper.stockDtoToStockResponse(stockDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockResponse> stockPatch(String itemId, StockPatchRequest stockPatchRequest) {
        StockDto updatedStock = stockService.update(
                itemId,
                stockPatchRequest.getName(),
                stockPatchRequest.getUnitType(),
                stockPatchRequest.getQuantity(),
                stockPatchRequest.getPricePerUnit(),
                stockPatchRequest.getDescription()
        );

        StockResponse response = StockMapper.stockDtoToStockResponse(updatedStock);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockResponse> stockPost(StockPostRequest stockPostRequest) {
        StockDto stockDto = stockService.addStock(stockPostRequest.getName(),
                stockPostRequest.getUnitType(),
                stockPostRequest.getQuantity(),
                stockPostRequest.getPricePerUnit(),
                stockPostRequest.getDescription());
        StockResponse stockResponse = StockMapper.stockDtoToStockResponse(stockDto);
        return new ResponseEntity<>(stockResponse, HttpStatus.CREATED);
    }
}