package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.ReportsApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.QuantitySoldList;
import de.hsrm.vegetables.service.services.PurchaseService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ReportsController implements ReportsApi {

    @NonNull
    private PurchaseService purchaseService;

    @Override
    public ResponseEntity<QuantitySoldList> soldItems(LocalDate fromDate, LocalDate toDate) {
        var list = purchaseService.getSoldItems(fromDate,toDate);
        var response = new QuantitySoldList();
        response.setItems(list);
        response.setFromDate(fromDate);
        response.setToDate(toDate);
        return ResponseEntity.ok(response);
    }
}
