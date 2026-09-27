package com.amber.splitsmart.user;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findTop8ByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
