package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.dto.model.task.EnableUserInteractionStateDto
import scraper.service.model.TaskUserInteraction
import scraper.service.repository.TaskUserInteractionRepository

@Service
class TaskUserInteractionService {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskUserInteractionRepository taskUserInteractionRepository

    TaskUserInteraction createOrUpdate(EnableUserInteractionStateDto dto) {
        def taskUserInteractionInDb = taskUserInteractionRepository.findByTaskId(dto.taskId)
        def taskUserInteraction = taskUserInteractionInDb.present
                ? taskUserInteractionInDb.get()
                : new TaskUserInteraction(
                    createdOnUtc: new Date(),
                    pipelineId: dto.pipelineId,
                    taskId: dto.taskId,
                    userId: dto.userId,
                    pageContext: dto.pageContext,
                )
        HashMap<String, Object> lastPageState = new HashMap<>()
        lastPageState.put('jpegScreenshotLink', dto.jpegScreenshotLink)
        lastPageState.put('pageElements', dto.pageElements)
        lastPageState.put('currentUrl', dto.currentUrl)
        taskUserInteraction.lastPageState = lastPageState
        taskUserInteraction.modifiedOnUtc = new Date()
        return taskUserInteractionRepository.save(taskUserInteraction)
    }
}
