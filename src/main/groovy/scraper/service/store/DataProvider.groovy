package scraper.service.store

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.core.pipeline.data.storage.FileStoreProvider

import javax.annotation.PostConstruct

@Service
class DataProvider implements FileStoreProvider {

    @Value('${minio.url}')
    String url

    @Value('${minio.accessKey}')
    String accessKey

    @Value('${minio.secretKey}')
    String secretKey

    private MinioClient minioClient

    @PostConstruct
    init() {
        minioClient = new MinioClient(url, accessKey, secretKey, true)
    }

    void createIfNotExistsBucket(String bucketName) {
        boolean isExist = minioClient.bucketExists(bucketName)
        if(isExist) {
            System.out.println("Bucket already exists.")
        } else {
            minioClient.makeBucket(bucketName)
        }
    }

    void setBucketLifeCycle(String bucketName, String lifeCycle) {
        minioClient.setBucketLifeCycle(bucketName, lifeCycle)
    }

    InputStream getObject(String bucketName, String objectName) {
        minioClient.getObject(bucketName, objectName)
    }

    void putJsonObject(String bucketName, String objectName, String data) {
        putObject(bucketName, objectName, data,  "application/json;charset=UTF-8")
    }

    void putObject(String bucketName, String objectName, String data, String contentType) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes("UTF-8"))
        minioClient.putObject(bucketName, objectName, bais, bais.available(), contentType)
    }

    void putObject(String bucketName, String objectName, byte[] data, String contentType) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data)
        minioClient.putObject(bucketName, objectName, bais, bais.available(), contentType)
    }

    String presignedGetObject(String bucketName, String objectName) {
        return minioClient.presignedGetObject(bucketName, objectName)
    }
}
