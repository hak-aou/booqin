package fr.uge.booqin.infra.persistence.repository.user;

import fr.uge.booqin.infra.persistence.entity.user.AuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, UUID> {

    @Query("SELECT a FROM AuthIdentity a JOIN UserEntity u ON a.id = u.authIdentityId WHERE u.email = :email")
    Optional<AuthIdentity> findByEmail(String email);

    @Query("SELECT a FROM AuthIdentity a JOIN UserEntity u ON a.id = u.authIdentityId WHERE u.username = :username")
    Optional<AuthIdentity> findByUsername(String username);

    @Query("SELECT a FROM AuthIdentity a JOIN UserEntity u ON a.id = u.authIdentityId WHERE u.id = :userId")
    Optional<AuthIdentity> findByUserId(UUID userId);
}
