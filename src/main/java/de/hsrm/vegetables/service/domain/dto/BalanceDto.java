package de.hsrm.vegetables.service.domain.dto;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BalanceDto {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false)
    private Float amount;
}
