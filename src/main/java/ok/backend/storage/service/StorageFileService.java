package ok.backend.storage.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.repository.StorageFileRepository;
import ok.backend.storage.dto.StorageUploadResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageFileService {

    private final StorageFileRepository storageFileRepository;

    @Value("${spring.servlet.multipart.location}")
    private String basePath;

    public StorageUploadResponseDto save(StorageFile storageFile){
        return new StorageUploadResponseDto(storageFileRepository.save(storageFile));
    }

    public List<StorageUploadResponseDto> findAllStorageFilesByStorageId(Long storageId) {
        return storageFileRepository.findAllStorageFilesByStorageId(storageId).stream()
                .map(StorageUploadResponseDto::new)
                .collect(Collectors.toList());
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
}
