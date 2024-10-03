package ok.backend.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageUploadResponseDto;
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

    @Operation(summary = "파일 업로드 API")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<StorageUploadResponseDto>> uploadFileToTeamStorage(@RequestParam Long ownerId,
                                                                            @RequestParam StorageType storageType,
                                                                            @RequestParam List<MultipartFile> files) throws IOException {
        List<StorageUploadResponseDto> storageUploadResponseDtos =  storageService.uploadFileToStorage(ownerId, storageType, files);

        return ResponseEntity.ok(storageUploadResponseDtos);
    }

    @Operation(summary = "파일 다운로드 API")
    @PostMapping(value = "/download")
    public ResponseEntity<Resource> download() throws MalformedURLException {
        UrlResource resource = new UrlResource("file:" + "/Users/sjpark/Desktop/Storage/profiles/1_profile_image.jpeg");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
