package de.hsrm.vegetables.service.domain.dto;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
// We need to frequently look up refreshTokens. Let's be nice and use an index
@Table(indexes = @Index(columnList = "refreshtoken"))
public class RefreshTokenDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "refreshtoken", length = 10000)
    private String refreshToken;

    @ManyToOne
    private UserDto user;

}
