package de.hsrm.vegetables.my_food_coop_service.domain.dto;

import de.hsrm.vegetables.my_food_coop_service.model.BalanceChangeType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BalanceHistoryItemDto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    // Tie this purchase to a user
    private UserDto userDto;

    @OneToOne
    // Tie this balance history item to the respective purchase if it resulted from such a one
    private PurchaseDto purchase;

    @Column
    private OffsetDateTime createdOn;

    @Column(nullable = false)
    private BalanceChangeType balanceChangeType;

    @Column(nullable = false)
    private Float amount;

    @PrePersist
    public void setCreationDateTime() {
        this.createdOn = OffsetDateTime.now();
    }
}
