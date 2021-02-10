package de.hsrm.vegetables.service.domain.dto;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class UserDto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String memberId;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private Float balance = 0f;

    @Column
    @Enumerated
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    private List<Role> roles;

}
