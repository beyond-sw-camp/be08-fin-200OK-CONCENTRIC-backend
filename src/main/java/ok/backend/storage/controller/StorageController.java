package ok.backend.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.dto.StorageStatusResponseDto;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Tag(name = "Storage", description = "파일 전송/저장")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/storage")
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
    public ResponseEntity<ByteArrayResource> downloadFileFromStorage(@RequestParam Long ownerId,
                                                            @RequestParam StorageType storageType,
                                                            @RequestParam Long storageFileId) throws MalformedURLException {

        return storageService.downloadFileFromStorage(ownerId, storageType, storageFileId);
    }

    @Operation(summary = "프로필 사진 요청 API", description = "프로필 사진을 요청하고 반환합니다.")
    @PostMapping(value = "/image/profile")
    public ResponseEntity<ByteArrayResource> getProfileImage(@RequestParam String path) throws MalformedURLException {
        return storageFileService.getProfileImage(path);
    }

    @Operation(summary = "사진 요청 API", description = "사진을 요청하고 반환합니다.")
    @PostMapping(value = "/image")
    public ResponseEntity<ByteArrayResource> getProfileImage(@RequestParam Long storageFileId) throws MalformedURLException {
        return storageFileService.getProfileImage(storageFileId);
    }

    @Operation(summary = "파일함을 조회하는 API")
    @GetMapping(value = "/")
    public ResponseEntity<StorageStatusResponseDto> getStorage(@RequestParam Long ownerId,
                                                         @RequestParam StorageType storageType){

        return ResponseEntity.ok(storageService.findStorage(ownerId, storageType));
    }

    @Operation(summary = "파일함의 모든 파일을 조회하는 API")
    @GetMapping(value = "/list")
    public ResponseEntity<List<StorageResponseDto>> getAllStorageFiles(@RequestParam Long ownerId,
                                                                       @RequestParam StorageType storageType) {

        return ResponseEntity.ok(storageService.findAllStorageFiles(ownerId, storageType));
    }

    @Operation(summary = "파일함의 파일 삭제 API", description = "파일함에서 파일을 삭제합니다.")
    @PostMapping(value = "/delete/file")
    public ResponseEntity<StorageStatusResponseDto> deleteStorageFile(@RequestParam Long ownerId,
                                                                      @RequestParam StorageType storageType,
                                                                      @RequestParam Long storageFileId) {

        return ResponseEntity.ok(storageService.deleteStorageFile(ownerId, storageType, storageFileId));
    }

}
