package ok.backend.member.domain.repository;

import ok.backend.member.domain.entity.Email;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends CrudRepository<Email, Long> {
    Optional<Email> findByEmail(String email);

    void deleteByEmail(String email);
}
