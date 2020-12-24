package de.hsrm.vegetables.service.domain.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * Example Class to show how DTO creation works
 *
 * Delete this file once the first functionality for the DB is implemented
 */


@Entity // Persists this class
@Table(name = "example_dto") // Could be omitted, this way we force the table name
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExampleDto {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    // @Column can be omitted, this way be force the name of the column
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private Integer value;
}
