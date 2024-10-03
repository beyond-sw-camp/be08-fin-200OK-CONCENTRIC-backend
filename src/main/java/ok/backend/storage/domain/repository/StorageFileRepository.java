package ok.backend.storage.domain.repository;

import ok.backend.storage.domain.entity.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageFileRepository extends JpaRepository<StorageFile, Long> {

    List<StorageFile> findAllStorageFilesByStorageId(Long storageId);
}
