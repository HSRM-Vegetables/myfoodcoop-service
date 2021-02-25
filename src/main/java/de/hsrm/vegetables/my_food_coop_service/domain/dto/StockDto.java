package de.hsrm.vegetables.my_food_coop_service.domain.dto;

import de.hsrm.vegetables.my_food_coop_service.model.OriginCategory;
import de.hsrm.vegetables.my_food_coop_service.model.StockStatus;
import de.hsrm.vegetables.my_food_coop_service.model.UnitType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private Float pricePerUnit = null;

    @Column(length = 10000)
    private String description;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private boolean sustainablyProduced = true;

    @ElementCollection
    private List<String> certificates = new ArrayList<>();

    @Column(nullable = false)
    private OriginCategory originCategory = OriginCategory.UNKNOWN;

    private String producer;

    @Column(nullable = false)
    private String supplier;

    private LocalDate orderDate;

    private LocalDate deliveryDate;

    @Column(nullable = false)
    private StockStatus stockStatus = StockStatus.ORDERED;

    @Column(nullable = false)
    private Float vat = 0.19f;

}
