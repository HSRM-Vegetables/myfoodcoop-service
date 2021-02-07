package de.hsrm.vegetables.service.domain.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PurchaseDto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column
    private OffsetDateTime createdOn;

    @Column(nullable = false)
    private Float totalPrice;

    @Column(nullable = false)
    private Float totalVat;

    @OneToMany
    private List<PurchasedItemDto> purchasedItems;

    @ManyToOne
    // Tie this purchase to a user
    private UserDto userDto;

    @PrePersist
    public void setCreationDateTime() {
        this.createdOn = OffsetDateTime.now();
    }
}
