package ok.backend.storage.domain.repository;

import ok.backend.storage.domain.entity.Storage;
import ok.backend.storage.domain.enums.StorageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    Optional<Storage> findByOwnerIdAndStorageType(Long ownerId, StorageType storageType);
}
