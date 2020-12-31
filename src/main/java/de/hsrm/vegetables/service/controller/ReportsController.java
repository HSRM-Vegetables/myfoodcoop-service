package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.ReportsApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.QuantitySoldList;
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

    @Override
    public ResponseEntity<QuantitySoldList> soldItems(LocalDate fromDate, LocalDate toDate) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
