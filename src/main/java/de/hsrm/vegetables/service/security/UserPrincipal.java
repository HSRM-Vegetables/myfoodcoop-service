package de.hsrm.vegetables.service.security;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserPrincipal {

    private String id;

    private String username;

    private List<Role> roles;

}
