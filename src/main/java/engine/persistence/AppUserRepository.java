package engine.persistence;

import engine.businesslayer.AppUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, Integer> {
    Optional<AppUser> findAppUserByUsername(String username);
}
