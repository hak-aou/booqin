package fr.uge.booqin.infra.persistence.entity.user;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Represent the data used by the authentication component
 * The domain doesn't have to know about this entity and the auth process.
 */
@Entity
public class AuthIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String password;

    public AuthIdentity(String password) {
        this.password = password;
    }

    public AuthIdentity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
