package de.hsrm.vegetables.my_food_coop_service.domain.dto;

import de.hsrm.vegetables.my_food_coop_service.model.UnitType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class PurchasedItemDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // This object needs an id, but that's not a value the user is ever gonna see
    private Long id;

    @ManyToOne
    // A purchase items relates to an item in the stock
    private StockDto stockDto;

    @Column(nullable = false)
    // how many items were purchased
    private Float amount;

    @Column(nullable = false)
    // Price at time of purchase
    private Float pricePerUnit;

    @Column(nullable = false)
    // UnitType at time of purchase
    private UnitType unitType;

    @Column(nullable = false)
    // VAT at time of purchase
    private Float vat;
}
