package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.ExampleDto;
import de.hsrm.vegetables.service.repositories.ExampleRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Example implementation of a service
 * This class is the connecting component between the controller and the repository
 * All business logic needs to be implemented here, not in the controller
 *
 * Delete me
 */

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class ExampleService {

    @NonNull
    private final ExampleRepository exampleRepository;

    public List<ExampleDto> list() {
        return exampleRepository.findAll();
    }

    public ExampleDto addExample(String name, Integer value) {
        ExampleDto exampleDto = new ExampleDto();
        exampleDto.setName(name);
        exampleDto.setValue(value);
        exampleRepository.save(exampleDto);
        return exampleDto;
    }

}
