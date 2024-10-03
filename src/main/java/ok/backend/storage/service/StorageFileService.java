package ok.backend.storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.repository.StorageFileRepository;
import ok.backend.storage.dto.StorageUploadResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageFileService {

    private final StorageFileRepository storageFileRepository;

    public StorageUploadResponseDto save(StorageFile storageFile){
        return new StorageUploadResponseDto(storageFileRepository.save(storageFile));
    }

    public List<StorageUploadResponseDto> findAllStorageFilesByStorageId(Long storageId) {
        return storageFileRepository.findAllStorageFilesByStorageId(storageId).stream()
                .map(StorageUploadResponseDto::new)
                .collect(Collectors.toList());
    }
}
