package turuq.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import turuq.backend.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    Optional<User> findByEmail(String email);

    Page<User> findByAge(Integer age, Pageable pageable);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, String id);
}
