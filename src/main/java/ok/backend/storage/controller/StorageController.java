package ok.backend.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageUploadResponseDto;
import ok.backend.storage.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Storage", description = "파일 전송/저장")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/file")
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "파일 업로드 API")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<StorageUploadResponseDto>> uploadFileToTeamStorage(@RequestParam Long ownerId,
                                                                            @RequestParam StorageType storageType,
                                                                            @RequestParam List<MultipartFile> files) throws IOException {
        List<StorageUploadResponseDto> storageUploadResponseDtos =  storageService.uploadFileToStorage(ownerId, storageType, files);

        return ResponseEntity.ok(storageUploadResponseDtos);
    }

}
