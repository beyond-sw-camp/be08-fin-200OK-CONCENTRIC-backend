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
import ok.backend.storage.dto.StorageStatusResponseDto;
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
import java.util.stream.Collectors;

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

        File file = new File(basePath + "/teams/" + storage.getOwnerId());
        file.mkdir();
    }

    public void createPrivateStorage(Long ownerId){
        Storage storage = Storage.builder()
                .ownerId(ownerId)
                .storageType(StorageType.PRIVATE)
                .capacity(1024 * 1024 * 100L)
                .currentSize(0L)
                .build();

        storageRepository.save(storage);

        File file = new File(basePath + "/private/" + storage.getOwnerId());
        file.mkdir();
    }

    public void createChatStorage(Long ownerId){
        Storage storage = Storage.builder()
                .ownerId(ownerId)
                .storageType(StorageType.CHAT)
                .capacity(1024 * 1024 * 100L)
                .currentSize(0L)
                .build();

        storageRepository.save(storage);

        File file = new File(basePath + "/chat/" + storage.getOwnerId());
        file.mkdir();
    }

    public List<StorageResponseDto> uploadFileToStorage(Long ownerId, StorageType storageType, List<MultipartFile> files) throws IOException {
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        Long totalSize = 0L;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        System.out.println(files.get(0).getSize());
//        System.out.println(totalSize + " " + storage.getCapacity());
        if(storageType.equals(StorageType.CHAT) && storage.getCapacity() < totalSize){
            throw new CustomException(ErrorCode.STORAGE_CAPACITY_EXCEED);
        } else if(storageType.equals(StorageType.CHAT) && storage.getCapacity() < storage.getCurrentSize() + totalSize){
            while(storage.getCapacity() < storage.getCurrentSize() + totalSize){
                Long size = storageFileService.deleteStorageFileByOrder(storage.getId());
                storage.updateStorageCurrentSize(-size);
            }
        } else if(storage.getCapacity() < storage.getCurrentSize() + totalSize){
            throw new CustomException(ErrorCode.STORAGE_CAPACITY_EXCEED);
        }

        List<StorageResponseDto> storageFiles = new ArrayList<>();

        String additionalPath = null;
        if(storageType.equals(StorageType.TEAM)){
            additionalPath = "/teams/";
        } else if(storageType.equals(StorageType.PRIVATE)){
            additionalPath = "/private/";
        }else if(storageType.equals(StorageType.CHAT)){
            additionalPath = "/chat/";
        }
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
                    .isActive(true)
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

        UrlResource resource = new UrlResource("file:" + storageFile.getPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageFile.getOriginalName() + "\"")
                .body(resource);
    }

    public StorageStatusResponseDto findStorage(Long ownerId, StorageType storageType) {
        return new StorageStatusResponseDto(this.findByOwnerIdAndStorageType(ownerId, storageType));
    }

    public List<StorageResponseDto> findAllStorageFiles(Long ownerId, StorageType storageType) {
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        return storageFileService.findAllStorageFilesByStorageId(storage.getId())
                .stream()
                .map(StorageResponseDto::new)
                .collect(Collectors.toList());
    }

    public void deletePrivateStorage(Long ownerId) {
        this.deleteStorage(ownerId, StorageType.PRIVATE);
    }

    public void deleteTeamStorage(Long ownerId) {
        this.deleteStorage(ownerId, StorageType.TEAM);
    }

    public void deleteChatStorage(Long ownerId) {
        this.deleteStorage(ownerId, StorageType.CHAT);
    }

    public void deleteStorage(Long ownerId, StorageType storageType) {
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        storageFileService.deleteAllStorageFiles(storage.getId());

        storageRepository.delete(storage);
    }

    public StorageStatusResponseDto deleteStorageFile(Long ownerId, StorageType storageType, Long storageFileId){
        Storage storage = this.findByOwnerIdAndStorageType(ownerId, storageType);

        Long size = storageFileService.deleteStorageFile(storage.getId(), storageFileId);

        storage.updateStorageCurrentSize(-size);

        return new StorageStatusResponseDto(storageRepository.save(storage));
    }


}
