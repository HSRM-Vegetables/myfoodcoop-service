package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.service.domain.dto.ExampleDto;
import de.hsrm.vegetables.service.services.ExampleService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Example controller to show how a controller uses a service
 * No business logic must be implemented here. We only combine different services here and handle errors
 *
 * Delete me
 */

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class ExampleDatabaseController {

    @NonNull
    private final ExampleService exampleService;

    @GetMapping(
            value="/examples"
    )
    public ResponseEntity<List<ExampleDto>> getAllExamples() {
        return ResponseEntity.ok(exampleService.list());
    }


    // This is really a crude hack in order to not create another class for the post body
    // Do not do that in production...
    @PostMapping(
            value="/examples"
    ) public ResponseEntity<ExampleDto> postExample(@RequestBody ExampleDto exampleDto) {
        ExampleDto res = exampleService.addExample(exampleDto.getName(), exampleDto.getValue());
        return ResponseEntity.ok(res);
    }

}
