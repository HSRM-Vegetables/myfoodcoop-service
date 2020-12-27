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
import org.springframework.web.bind.annotation.*;

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
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AllStockResponse> stockGet() {
        List<StockResponse> items = StockMapper.listStockDtoToListStockResponse(stockService.getAll());
        AllStockResponse response = new AllStockResponse();
        response.setItems(items);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockResponseById> stockItemGet(String itemId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @Override
    public ResponseEntity<StockResponse> stockPatch(String itemId, StockPatchRequest stockPatchRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<StockResponse> stockPost(StockPostRequest stockPostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
