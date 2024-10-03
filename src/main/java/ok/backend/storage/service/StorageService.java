package ok.backend.storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.storage.domain.entity.Storage;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.domain.repository.StorageRepository;
import ok.backend.storage.dto.StorageResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageService {

    private final StorageRepository storageRepository;

    private final StorageFileService storageFileService;

    @Value("${spring.servlet.multipart.location}")
    private String basePath;

    public Storage findByOwnerIdAndStorageType(Long ownerId, StorageType storageType) {
        return storageRepository.findByOwnerIdAndStorageType(ownerId, storageType).orElseThrow(() ->
                new CustomException(ErrorCode.STORAGE_NOT_FOUND));
    }

    public void createTeamStorage(Long ownerId){
        Storage storage = Storage.builder()
                .ownerId(ownerId)
                .storageType(StorageType.TEAM)
                .capacity(1024 * 1024 * 500L)
                .currentSize(0L)
                .build();

        storageRepository.save(storage);
    }

    public void createPrivateStorage(Long ownerId){
        Storage storage = Storage.builder()
                .ownerId(ownerId)
                .storageType(StorageType.PRIVATE)
                .capacity(1024 * 1024 * 100L)
                .currentSize(0L)
                .build();

        storageRepository.save(storage);
    }

    public List<StorageResponseDto> uploadFileToStorage(Long ownerId, StorageType storageType, List<MultipartFile> files) throws IOException {
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        Long totalSize = 0L;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        System.out.println(totalSize + " " + storage.getCapacity());
        if(storage.getCapacity() < storage.getCurrentSize() + totalSize){
            throw new CustomException(ErrorCode.STORAGE_CAPACITY_EXCEED);
        }

        List<StorageResponseDto> storageFiles = new ArrayList<>();

        String additionalPath = storageType.equals(StorageType.TEAM) ? "/teams/" : "/private/";
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = uuid + extension;
            String savedPath = basePath + additionalPath + ownerId + "/" + savedName;
            Long size = file.getSize();

            StorageFile storageFile = StorageFile.builder()
                    .storage(storage)
                    .name(savedName)
                    .originalName(originalName)
                    .path(savedPath)
                    .size(size)
                    .build();

            storageFiles.add(storageFileService.save(storageFile));
            storage.updateStorageCurrentSize(size);

            file.transferTo(new File(savedPath));
        }

        return storageFiles;
    }

    public ResponseEntity<Resource> downloadFileFromStorage(Long ownerId, StorageType storageType, Long storageFileId) throws MalformedURLException {
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        StorageFile storageFile = storageFileService.findByStorageIdAndId(storage.getId(), storageFileId);

        System.out.println(storageFile.getOriginalName());
        UrlResource resource = new UrlResource("file:" + storageFile.getPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageFile.getOriginalName() + "\"")
                .body(resource);
    }
}
