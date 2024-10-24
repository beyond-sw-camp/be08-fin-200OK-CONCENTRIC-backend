package ok.backend.storage.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.storage.domain.entity.Storage;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.domain.repository.StorageFileRepository;
import ok.backend.storage.domain.repository.StorageRepository;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.dto.StorageStatusResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import static ok.backend.common.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageFileRepository storageFileRepository;

    @Mock
    private StorageFileService storageFileService;

    @Mock
    private AwsFileService awsFileService;

    private Storage storage;
    private StorageFile storageFile;
    private MultipartFile multipartFile;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        storage = mock(Storage.class);
        storageFile = mock(StorageFile.class);
        multipartFile = mock(MultipartFile.class);

        when(storage.getId()).thenReturn(1L);
        when(storage.getCapacity()).thenReturn(500L);
        when(storage.getCurrentSize()).thenReturn(100L);

        when(storageFile.getStorage()).thenReturn(storage);
        when(storageFile.getId()).thenReturn(1L);
        when(storageFile.getPath()).thenReturn("path/to/file.txt");
        when(storageFile.getSize()).thenReturn(50L);
        when(storageFile.getOriginalName()).thenReturn("file.txt");

        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getSize()).thenReturn(50L);


        when(storageRepository.findByOwnerIdAndStorageType(1L, StorageType.TEAM))
                .thenReturn(Optional.of(storage));
        when(storageFileRepository.save(storageFile)).thenReturn(storageFile);

        when(storageFileService.findByStorageIdAndId(storage.getId(), storageFile.getId()))
                .thenReturn(storageFile);
    }

    @Test
    @DisplayName("파일 업로드 - 성공")
    void uploadFileToStorage_success() throws IOException {
        // given
        ArgumentCaptor<StorageFile> storageFileCaptor = ArgumentCaptor.forClass(StorageFile.class);

        // StorageFile 객체를 캡처하고 StorageResponseDto 반환
        when(storageFileService.save(storageFileCaptor.capture())).thenAnswer(invocation -> {
            StorageFile capturedFile = storageFileCaptor.getValue();
            return new StorageResponseDto(capturedFile);
        });

        doNothing().when(awsFileService).uploadFileByPath(any(MultipartFile.class), anyString());

        // when
        List<StorageResponseDto> storageResponseDtos = storageService.uploadFileToStorage(1L, StorageType.TEAM, List.of(multipartFile));

        // then
        assertNotNull(storageResponseDtos);
        assertEquals(1, storageResponseDtos.size());

        StorageFile capturedStorageFile = storageFileCaptor.getValue();
        assertNotNull(capturedStorageFile);
        assertEquals("file.txt", capturedStorageFile.getOriginalName());
        assertEquals(50L, capturedStorageFile.getSize());

        verify(storageFileService, times(1)).save(any(StorageFile.class));
        verify(awsFileService, times(1)).uploadFileByPath(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("파일 업로드 - 파일함 용량 초과")
    void uploadFileToStorage_exceedCapacity() throws IOException {
        // given
        when(multipartFile.getSize()).thenReturn(500L);
        ArgumentCaptor<StorageFile> storageFileCaptor = ArgumentCaptor.forClass(StorageFile.class);

        // StorageFile 객체를 캡처하고 StorageResponseDto 반환
        when(storageFileService.save(storageFileCaptor.capture())).thenAnswer(invocation -> {
            StorageFile capturedFile = storageFileCaptor.getValue();
            return new StorageResponseDto(capturedFile);
        });

        doNothing().when(awsFileService).uploadFileByPath(any(MultipartFile.class), anyString());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageService.uploadFileToStorage(1L, StorageType.TEAM, List.of(multipartFile));
        });

        // then
        assertEquals(STORAGE_CAPACITY_EXCEED, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일 다운로드 - 성공")
    void downloadFileFromStorage_success() throws MalformedURLException {
        // given
        when(awsFileService.downloadFile(storageFile.getPath())).thenReturn(new byte[10]);

        // when
        ResponseEntity<ByteArrayResource> byteArrayResource = storageService.downloadFileFromStorage(1L, StorageType.TEAM, storageFile.getId());

        // then
        assertNotNull(byteArrayResource);
        assertEquals(200, byteArrayResource.getStatusCodeValue());
        assertInstanceOf(ByteArrayResource.class, byteArrayResource.getBody());
    }

    @Test
    @DisplayName("파일 다운로드 - 파일을 찾을 수 없음")
    void downloadFileFromStorage_fileNotMatched() throws MalformedURLException {
        // given
        when(storageFileService.findByStorageIdAndId(storage.getId(), storageFile.getId()))
                .thenThrow(new CustomException(ErrorCode.STORAGE_FILE_NOT_MATCHED));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            // 잘못된 파일 id 요청
            storageService.downloadFileFromStorage(1L, StorageType.TEAM, storageFile.getId());
        });

        // then
        assertEquals(STORAGE_FILE_NOT_MATCHED, exception.getErrorCode());
    }


    @Test
    @DisplayName("파일함 조회 - 성공")
    void findStorage_success() {
        // when
        StorageStatusResponseDto storageStatusResponseDto = storageService.findStorage(1L, StorageType.TEAM);

        // then
        assertNotNull(storageStatusResponseDto);
        verify(storageRepository, times(1)).findByOwnerIdAndStorageType(1L, StorageType.TEAM);
    }

    @Test
    @DisplayName("파일함 조회 - 파일함를 찾을 수 없음")
    void findStorage_storageNotMatched() {
        // given
        when(storageService.findByOwnerIdAndStorageType(1L, StorageType.TEAM))
                .thenThrow(new CustomException(ErrorCode.STORAGE_NOT_FOUND));;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageService.findStorage(1L, StorageType.TEAM);
        });

        // then
        assertEquals(STORAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일함의 모든 파일 조회 - 성공")
    void findAllStorageFiles_success() {
        // given
        ArgumentCaptor<Long> storageIdCaptor = ArgumentCaptor.forClass(Long.class);
        when(storageFileService.findAllStorageFilesByStorageId(storageIdCaptor.capture()))
                .thenReturn(List.of(storageFile));

        // when
        List<StorageResponseDto> storageResponseDtos = storageService.findAllStorageFiles(1L, StorageType.TEAM);

        // then
        assertNotNull(storageResponseDtos);
        assertEquals(1, storageResponseDtos.size());

        Long capturedStorageId = storageIdCaptor.getValue();
        assertNotNull(capturedStorageId);

        assertEquals("file.txt", storageResponseDtos.get(0).getOriginalName());
        assertEquals(50L, storageResponseDtos.get(0).getSize());

        verify(storageFileService, times(1)).findAllStorageFilesByStorageId(storage.getId());
    }

    @Test
    @DisplayName("파일함의 모든 파일 조회 - 파일함를 찾을 수 없음")
    void findAllStorageFiles_storageNotMatched() {
        // given
        when(storageService.findByOwnerIdAndStorageType(1L, StorageType.TEAM))
                .thenThrow(new CustomException(ErrorCode.STORAGE_NOT_FOUND));;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageService.findAllStorageFiles(1L, StorageType.TEAM);
        });

        // then
        assertEquals(STORAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일 삭제 - 성공")
    void deleteStorageFile_success() {
        // given
        when(storageFileService.deleteStorageFile(storage.getId(), storageFile.getId())).thenReturn(50L);
        when(storageRepository.save(storage)).thenReturn(storage);

        // when
        StorageStatusResponseDto storageStatusResponseDto = storageService.deleteStorageFile(1L, StorageType.TEAM, storageFile.getId());

        // then
        assertNotNull(storageStatusResponseDto);
        verify(storageFileService, times(1)).deleteStorageFile(storage.getId(), storageFile.getId());
        verify(storageRepository, times(1)).save(storage);
    }

    @Test
    @DisplayName("파일 삭제 - 파일함를 찾을 수 없음")
    void deleteStorageFile_storageNotMatched() {
        // given
        when(storageService.findByOwnerIdAndStorageType(1L, StorageType.TEAM))
                .thenThrow(new CustomException(ErrorCode.STORAGE_NOT_FOUND));;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageService.deleteStorageFile(1L, StorageType.TEAM, storageFile.getId());
        });

        // then
        assertEquals(STORAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일 삭제 - 파일을 찾을 수 없음")
    void deleteStorageFile_fileNotMatched() {
        // given
        when(storageFileService.deleteStorageFile(storage.getId(), storageFile.getId()))
                .thenThrow(new CustomException(ErrorCode.STORAGE_FILE_NOT_MATCHED));;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageService.deleteStorageFile(1L, StorageType.TEAM, storageFile.getId());
        });

        // then
        assertEquals(STORAGE_FILE_NOT_MATCHED, exception.getErrorCode());
    }
}
