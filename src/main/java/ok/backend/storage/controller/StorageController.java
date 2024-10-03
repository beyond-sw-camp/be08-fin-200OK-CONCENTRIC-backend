package ok.backend.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Tag(name = "Storage", description = "파일 전송/저장")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/file")
public class StorageController {

    private final StorageService storageService;
    private final StorageFileService storageFileService;

    @Operation(summary = "파일 업로드 API", description = "파일을 업로드하고 저장한 파일의 정보를 반환합니다.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<StorageResponseDto>> uploadFileToStorage(@RequestParam Long ownerId,
                                                                        @RequestParam StorageType storageType,
                                                                        @RequestParam List<MultipartFile> files) throws IOException {
        List<StorageResponseDto> storageUploadResponseDtos =  storageService.uploadFileToStorage(ownerId, storageType, files);

        return ResponseEntity.ok(storageUploadResponseDtos);
    }

    @Operation(summary = "파일 다운로드 API", description = "요청한 파일을 반환합니다.")
    @PostMapping(value = "/download")
    public ResponseEntity<Resource> downloadFileFromStorage(@RequestParam Long ownerId,
                                                            @RequestParam StorageType storageType,
                                                            @RequestParam Long storageFileId) throws MalformedURLException {

        return storageService.downloadFileFromStorage(ownerId, storageType, storageFileId);
    }

    @PostMapping(value = "/image/profile")
    public ResponseEntity<Resource> getProfileImage(@RequestParam String path) throws MalformedURLException {
        return storageFileService.getProfileImage(path);
    }

}
