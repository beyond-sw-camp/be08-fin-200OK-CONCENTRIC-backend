package ok.backend.storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.domain.repository.StorageFileRepository;
import ok.backend.storage.dto.StorageResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageFileService {

    private final StorageFileRepository storageFileRepository;

    @Value("${spring.servlet.multipart.location}")
    private String basePath;

    public StorageResponseDto save(StorageFile storageFile){
        return new StorageResponseDto(storageFileRepository.save(storageFile));
    }

    public List<StorageFile> findAllStorageFilesByStorageId(Long storageId) {
        return storageFileRepository.findAllStorageFilesByStorageId(storageId);
    }

    public StorageFile findByStorageIdAndId(Long storageId, Long storageFileId) {
        return storageFileRepository.findByStorageIdAndId(storageId, storageFileId).orElseThrow(() ->
                new CustomException(ErrorCode.STORAGE_FILE_NOT_MATCHED));
    }

    public String saveProfileImage(Long memberId, String previous, MultipartFile file) throws IOException {
        if(previous != null){
            File previousFile = new File(previous);
            boolean fileDeleted = previousFile.delete();
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedPath = basePath + "/profiles/" + memberId + "_profile_image" + extension;

        file.transferTo(new File(savedPath));

        return savedPath;
    }

    public ResponseEntity<Resource> getProfileImage(String path) throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + path);

        String extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));

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

        storageFiles.forEach((storageFile) -> new File(storageFile.getPath()).delete());

        storageFileRepository.deleteAll(storageFiles);
    }

    public Long deleteStorageFile(Long storageId, Long storageFileId) {
        StorageFile storageFile = this.findByStorageIdAndId(storageId, storageFileId);

        new File(storageFile.getPath()).delete();

        storageFileRepository.delete(storageFile);

        return storageFile.getSize();
    }
}
