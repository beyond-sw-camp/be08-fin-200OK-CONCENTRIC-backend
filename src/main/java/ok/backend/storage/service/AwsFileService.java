package ok.backend.storage.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsFileService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String dirName) {
        File fileObj = convertFile(file);
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = dirName + UUID.randomUUID().toString() + extension;

        log.info("upload file: {}", fileName);
        amazonS3Client.putObject(bucket, fileName, fileObj);
        fileObj.delete();
//        return amazonS3Client.getUrl(bucket, fileName).toString();
        return fileName;
    }

    public void uploadFileByPath(MultipartFile file, String fileName) {
        File fileObj = convertFile(file);

        log.info("upload file: {}", fileName);
        amazonS3Client.putObject(bucket, fileName, fileObj);
        fileObj.delete();
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = amazonS3Client.getObject(bucket, fileName);
        log.info(s3Object.getKey());
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e){
            throw new IllegalStateException("Cannot download file", e);
        }
    }

    public void deleteFile(String fileName){
        amazonS3Client.deleteObject(bucket, fileName);
        log.info("delete file: {}", fileName);
    }

    public File convertFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return convertedFile;
    }
}
