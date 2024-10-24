package ok.backend.storage.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.storage.domain.entity.Storage;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.repository.StorageFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StorageFileServiceTest {
    @Mock
    private StorageFileRepository storageFileRepository;

    @Mock
    private AwsFileService awsFileService;

    private Storage storage;
    private StorageFile storageFile;
    private StorageFile newStorageFile;

    private MultipartFile multipartFile;

    @InjectMocks
    private StorageFileService storageFileService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        storageFile = mock(StorageFile.class);
        newStorageFile = mock(StorageFile.class);

        when(storageFile.getStorage()).thenReturn(storage);
        when(storageFile.getId()).thenReturn(1L);
        when(storageFile.getPath()).thenReturn("path/to/file.jpg");
        when(storageFile.getSize()).thenReturn(50L);
        when(storageFile.getOriginalName()).thenReturn("file.jpg");

        when(newStorageFile.getId()).thenReturn(2L);
        when(newStorageFile.getPath()).thenReturn("path/to/file.txt");
        when(newStorageFile.getSize()).thenReturn(100L);

        when(storageFileRepository.findById(anyLong())).thenReturn(Optional.of(storageFile));
        when(storageFileRepository.save(storageFile)).thenReturn(storageFile);
    }

    @Test
    @DisplayName("프로필 이미지 요청 - 성공")
    void getProfileImage_success() throws MalformedURLException {
        // given
        byte[] imageData = new byte[10];
        when(awsFileService.downloadFile(anyString())).thenReturn(imageData);

        // when
        ResponseEntity<ByteArrayResource> response = storageFileService.getProfileImage(storageFile.getPath());

        // then
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        verify(awsFileService).downloadFile("path/to/file.jpg");
    }

    @Test
    @DisplayName("프로필 이미지 요청 - 파일을 찾을 수 없음")
    void getProfileImage_notFound() throws MalformedURLException {
        // given
        when(awsFileService.downloadFile(anyString())).thenReturn(null);

        // when
        ResponseEntity<ByteArrayResource> response = storageFileService.getProfileImage(storageFile.getPath());

        // then
        assertEquals(ResponseEntity.notFound().build(), response);
        verify(awsFileService).downloadFile("path/to/file.jpg");
        }

    @Test
    @DisplayName("이미지 요청 - 성공")
    void getImage_success() throws MalformedURLException {
        // given
        byte[] imageData = new byte[10];
        when(awsFileService.downloadFile(anyString())).thenReturn(imageData);

        // when
        ResponseEntity<ByteArrayResource> response = storageFileService.getProfileImage(storageFile.getId());

        // then
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        verify(awsFileService).downloadFile("path/to/file.jpg");
    }

    @Test
    @DisplayName("이미지 요청 - 파일을 찾을 수 없음")
    void getImage_notFound() {
        // given
        when(storageFileRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageFileService.getProfileImage(storageFile.getId());
        });

        // then
        assertEquals(ErrorCode.STORAGE_FILE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("파일을 정렬하여 삭제 - 성공")
    void deleteStorageFileByOrder_success() {
        // given
        when(storageFileRepository.findTop1ByStorageIdAndIsActiveTrueOrderByCreateDateAsc(1L))
                .thenReturn(Optional.of(newStorageFile));

        // captor 설정
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        // when
        Long deletedFileSize = storageFileService.deleteStorageFileByOrder(1L);

        // then
        assertEquals(100L, deletedFileSize);
        verify(awsFileService).deleteFile(stringCaptor.capture());
        assertEquals("path/to/file.txt", stringCaptor.getValue());
        verify(newStorageFile).updateStatus(false);
        verify(storageFileRepository).save(newStorageFile);
    }

    @Test
    @DisplayName("파일을 정렬하여 삭제 - 파일을 찾을 수 없음")
    void deleteStorageFileByOrder_storageNotMatched() {
        // given
        when(storageFileRepository.findTop1ByStorageIdAndIsActiveTrueOrderByCreateDateAsc(1L))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storageFileService.deleteStorageFileByOrder(1L);
        });

        // then
        assertEquals(ErrorCode.STORAGE_FILE_NOT_MATCHED, exception.getErrorCode());
    }
}