package org.benetech.servicenet.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomSpringSecurityUser extends User {

    private UUID id;

    public CustomSpringSecurityUser(String username, String password,
        Collection<? extends GrantedAuthority> authorities, UUID id) {
        super(username, password, authorities);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
