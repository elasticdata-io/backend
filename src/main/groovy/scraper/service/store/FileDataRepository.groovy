package scraper.service.store

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.core.pipeline.data.storage.FileBucketMapper
import scraper.core.pipeline.data.storage.FileStoreProvider
import scraper.service.model.Task

@Service
class FileDataRepository {

    @Autowired
    FileStoreProvider fileStoreProvider

    InputStream getDataFileToStream(Task task) {
        def config = FileBucketMapper.forJsonData(task.userId, task.id)
        return fileStoreProvider.getObject(config.bucketName, config.objectName)
    }

    String getDataFileToString(Task task) {
        def stream = getDataFileToStream(task)
        def mapper = new ObjectMapper()
        return mapper.readValue(stream, String)
    }

    List getDataFileToList(Task task) {
        def stream = getDataFileToStream(task)
        def mapper = new ObjectMapper()
        return mapper.readValue(stream, List)
    }
}
