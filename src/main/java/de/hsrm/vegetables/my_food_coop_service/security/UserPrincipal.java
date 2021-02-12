package de.hsrm.vegetables.my_food_coop_service.security;

import de.hsrm.vegetables.my_food_coop_service.model.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserPrincipal {

    private String id;

    private String username;

    private List<Role> roles;

}
