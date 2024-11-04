package ok.backend.member.domain.repository;

import ok.backend.member.domain.entity.Invite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteRepository extends CrudRepository<Invite, Long> {
    Optional<Invite> findById(String id);
}
