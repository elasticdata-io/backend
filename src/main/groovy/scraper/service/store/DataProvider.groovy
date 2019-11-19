package scraper.service.store

import io.minio.MinioClient
import io.minio.ServerSideEncryption
import io.minio.errors.MinioException
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

    getMinioClient() {
        return new MinioClient(url, accessKey, secretKey, true)
    }

    void createIfNotExistsBucket(String bucketName) {
        MinioClient minioClient = getMinioClient()
        boolean isExist = minioClient.bucketExists(bucketName)
        if(isExist) {
            System.out.println("Bucket already exists.")
        } else {
            minioClient.makeBucket(bucketName, null, true)
            /**
            ObjectLockConfiguration config = new ObjectLockConfiguration(
                    RetentionMode.COMPLIANCE,
                    7,
                    DurationUnit.DAYS
            )
            minioClient.setDefaultRetention(bucketName, config)**/
        }
    }

    void setBucketLifeCycle(String bucketName, String lifeCycle) {
        MinioClient minioClient = getMinioClient()
        minioClient.setBucketLifeCycle(bucketName, lifeCycle)
    }

    InputStream getObject(String bucketName, String objectName) {
        MinioClient minioClient = getMinioClient()
        minioClient.getObject(bucketName, objectName)
    }

    void putJsonObject(String bucketName, String objectName, String data) {
        putObject(bucketName, objectName, data,  "application/json;charset=UTF-8")
    }

    void putObject(String bucketName, String objectName, String data, String contentType) {
        MinioClient minioClient = getMinioClient()
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes("UTF-8"))
        long size = bais.available() as long
        HashMap headerMap = new HashMap()
        ServerSideEncryption sse = null
        minioClient.putObject(bucketName, objectName, bais, size, headerMap, sse, contentType)
        bais.close()
    }

    void putObject(String bucketName, String objectName, byte[] data, String contentType) {
        MinioClient minioClient = getMinioClient()
        ByteArrayInputStream bais = new ByteArrayInputStream(data)
        long size = bais.available() as long
        HashMap headerMap = new HashMap()
        ServerSideEncryption sse = null
        minioClient.putObject(bucketName, objectName, bais, size, headerMap, sse, contentType)
        bais.close()
    }

    String presignedGetObject(String bucketName, String objectName) {
        MinioClient minioClient = getMinioClient()
        try {
            return minioClient.presignedGetObject(bucketName, objectName)
        } catch(MinioException e) {
            logger.error("presignedGetObject fails...")
            logger.error(e)
        }
    }
}
