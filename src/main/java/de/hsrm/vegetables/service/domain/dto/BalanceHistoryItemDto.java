package de.hsrm.vegetables.service.domain.dto;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceChangeType;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.ChangeType;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.UnitType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BalanceHistoryItemDto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column
    private OffsetDateTime createdOn;

    @Column(nullable = false)
    private BalanceChangeType balanceChangeType;

    @Column(nullable = false)
    private Float amount;

    @ManyToOne
    // Tie this balance history item to a balance and thus to a user
    private BalanceDto balanceDto;

    @PrePersist
    public void setCreationDateTime() {
        this.createdOn = OffsetDateTime.now();
    }
}
