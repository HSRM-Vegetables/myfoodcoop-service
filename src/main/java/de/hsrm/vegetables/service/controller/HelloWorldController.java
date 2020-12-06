package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.HelloWorldApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.HelloWorldResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class HelloWorldController implements HelloWorldApi {

    @Override
    public ResponseEntity<HelloWorldResponse> helloWorld() {
        HelloWorldResponse response = new HelloWorldResponse();
        response.setMessage("Hello World!");
        return ResponseEntity.ok(response);
    }

}
