package ok.backend.storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.repository.StorageFileRepository;
import ok.backend.storage.dto.StorageResponseDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageFileService {

    private final StorageFileRepository storageFileRepository;

    private final AwsFileService awsFileService;

    public StorageResponseDto save(StorageFile storageFile){
        return new StorageResponseDto(storageFileRepository.save(storageFile));
    }

    public List<StorageFile> findAllStorageFilesByStorageId(Long storageId) {
        return storageFileRepository.findAllStorageFilesByStorageIdAndIsActiveTrue(storageId);
    }

    public StorageFile findByStorageIdAndId(Long storageId, Long storageFileId) {
        return storageFileRepository.findByStorageIdAndIdAndIsActiveTrue(storageId, storageFileId).orElseThrow(() ->
                new CustomException(ErrorCode.STORAGE_FILE_NOT_MATCHED));
    }

    public String saveProfileImage(Long memberId, String previous, MultipartFile file) throws IOException {
        if(previous != null){
            awsFileService.deleteFile(previous);
        }

        String dir = "profiles/" + memberId + "/";

        return awsFileService.uploadFile(file, dir);
    }

    public ResponseEntity<ByteArrayResource> getProfileImage(String path) throws MalformedURLException {
        ByteArrayResource resource = new ByteArrayResource(awsFileService.downloadFile(path));
        String extension = path.substring(path.lastIndexOf("."));

        if(".jpg".equals(extension) || ".jpeg".equals(extension)){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/jpeg"))
                    .body(resource);
        } else if(".png".equals(extension)){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/png"))
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }

    public void deleteAllStorageFiles(Long storageId) {
        List<StorageFile> storageFiles = this.findAllStorageFilesByStorageId(storageId);

        storageFiles.forEach((storageFile) -> awsFileService.deleteFile(storageFile.getPath()));

        storageFiles.forEach((storageFile) -> storageFile.updateStatus(false));

        storageFileRepository.saveAll(storageFiles);
    }

    public Long deleteStorageFile(Long storageId, Long storageFileId) {
        StorageFile storageFile = this.findByStorageIdAndId(storageId, storageFileId);

        awsFileService.deleteFile(storageFile.getPath());
        storageFile.updateStatus(false);

        storageFileRepository.save(storageFile);

        return storageFile.getSize();
    }

    public Long deleteStorageFileByOrder(Long storageId) {
        StorageFile storageFile = storageFileRepository
                .findTop1ByStorageIdAndIsActiveTrueOrderByCreateDateAsc(storageId).orElseThrow(() ->
                        new CustomException(ErrorCode.STORAGE_FILE_NOT_MATCHED));

        awsFileService.deleteFile(storageFile.getPath());

        storageFile.updateStatus(false);

        storageFileRepository.save(storageFile);

        return storageFile.getSize();
    }
}
