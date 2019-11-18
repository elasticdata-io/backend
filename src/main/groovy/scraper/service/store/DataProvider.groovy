package scraper.service.store

import io.minio.MinioClient
import io.minio.ObjectStat
import io.minio.errors.MinioException
import io.minio.messages.DurationUnit
import io.minio.messages.ObjectLockConfiguration
import io.minio.messages.RetentionMode
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.core.pipeline.data.storage.FileStoreProvider

import javax.annotation.PostConstruct

@Service
class DataProvider implements FileStoreProvider {
    private Logger logger = LogManager.getRootLogger()

    @Value('${minio.url}')
    String url

    @Value('${minio.publicUrl}')
    String publicUrl

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
            ObjectLockConfiguration config = new ObjectLockConfiguration(
                    RetentionMode.COMPLIANCE,
                    7,
                    DurationUnit.DAYS
            )
            minioClient.setDefaultRetention(bucketName, config)
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
        bais.close()
    }

    @Override
    void putFileObject(String bucketName, String objectName, String filename, String contentType) {
        File file = new File(filename)
        if (file.exists()) {
            minioClient.putObject(bucketName, objectName, filename, contentType)
            file.delete()
        }
    }

    void putObject(String bucketName, String objectName, byte[] data, String contentType) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data)
        minioClient.putObject(bucketName, objectName, bais, bais.available(), contentType)
        bais.close()
    }

    String presignedGetObject(String bucketName, String objectName) {
        try {
            minioClient.statObject(bucketName, objectName)
            return minioClient.presignedGetObject(bucketName, objectName)
        } catch(MinioException e) {
            logger.error("presignedGetObject fails...")
            logger.error(e)
        }
    }
}
