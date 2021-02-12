package de.hsrm.vegetables.my_food_coop_service.domain.dto;

import de.hsrm.vegetables.my_food_coop_service.model.UnitType;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class DisposedDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    // A disposed items relates to an item in the stock
    private StockDto stockDto;

    @ManyToOne
    // Who disposed this item
    private UserDto userDto;

    @Column
    private OffsetDateTime createdOn;

    @Column(nullable = false)
    // how many items were disposed
    private Float amount;

    @Column(nullable = false)
    // Price at time of disposal
    private Float pricePerUnit;

    @Column(nullable = false)
    // UnitType at time of disposal
    private UnitType unitType;

    @Column(nullable = false)
    // VAT at time of disposal
    private Float vat;

    @PrePersist
    public void setCreationDateTime() {
        this.createdOn = OffsetDateTime.now();
    }

}
