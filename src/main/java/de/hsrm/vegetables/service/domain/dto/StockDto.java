package de.hsrm.vegetables.service.domain.dto;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.StockStatus;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.UnitType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity // Persists this class
@EntityListeners(AuditingEntityListener.class)
@Data
public class StockDto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UnitType unitType;

    @Column(nullable = false)
    private Float quantity;

    @Column(nullable = false)
    private Float pricePerUnit;

    @Column(length = 10000)
    private String description;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private StockStatus stockStatus = StockStatus.ORDERED;

}
