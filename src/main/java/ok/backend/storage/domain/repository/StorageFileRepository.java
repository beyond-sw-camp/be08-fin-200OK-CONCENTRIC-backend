package ok.backend.storage.domain.repository;

import ok.backend.storage.domain.entity.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageFileRepository extends JpaRepository<StorageFile, Long> {

    List<StorageFile> findAllStorageFilesByStorageIdAndIsActiveTrue(Long storageId);

    Optional<StorageFile> findByStorageIdAndIdAndIsActiveTrue(Long storageId, Long storageFileId);

    Optional<StorageFile> findTop1ByStorageIdAndIsActiveTrueOrderByCreateDateAsc(Long storageId);
}
