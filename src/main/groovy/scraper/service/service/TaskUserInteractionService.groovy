package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.amqp.dto.DisableUserInteractionDto
import scraper.service.amqp.producer.TaskUserInteractionProducer
import scraper.service.dto.mapper.UserInteractionMapper
import scraper.service.dto.model.task.UserInteractionStateDto
import scraper.service.dto.model.task.UserInteractionDto
import scraper.service.model.TaskUserInteraction
import scraper.service.repository.TaskUserInteractionRepository
import scraper.service.ws.TaskWebsocketProducer

@Service
class TaskUserInteractionService {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskUserInteractionRepository taskUserInteractionRepository

    @Autowired
    TaskUserInteractionProducer taskUserInteractionProducer

    @Autowired
    TaskWebsocketProducer taskWebsocketProducer

    TaskUserInteraction createOrUpdate(UserInteractionStateDto dto) {
        def taskUserInteractionInDb = findByDto(dto)
        def taskUserInteraction = taskUserInteractionInDb
                ? taskUserInteractionInDb
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
        lastPageState.put('pageWidthPx', dto.pageWidthPx)
        lastPageState.put('pageHeightPx', dto.pageHeightPx)
        taskUserInteraction.lastPageState = lastPageState
        taskUserInteraction.modifiedOnUtc = new Date()
        if (dto.timeoutSeconds) {
            Calendar c = Calendar.getInstance()
            c.setTime(new Date())
            c.add(Calendar.SECOND, dto.timeoutSeconds as int)
            taskUserInteraction.expiredOnUtc = c.time
        }
        taskUserInteractionRepository.save(taskUserInteraction)
        notifyChanged(taskUserInteraction)
        return taskUserInteraction
    }

    TaskUserInteraction findById(String id) {
        def option = taskUserInteractionRepository.findById(id)
        if(option.present) {
            return option.get()
        }
    }

    List<UserInteractionDto> list(String taskId) {
        def list = taskUserInteractionRepository.findByTaskId(taskId)
        return list.collect { UserInteractionMapper.toUserInteractionDto(it) }
    }

    void disable(String interactionId) {
        def optional = taskUserInteractionRepository.findById(interactionId)
        if (!optional.present) {
            throw new Exception("interaction with id:${interactionId} not found")
        }
        def interaction = optional.get()
        def dto = new DisableUserInteractionDto(
            taskId: interaction.taskId,
            pageContext: interaction.pageContext,
            interactionId: interaction.id,
        )
        taskUserInteractionProducer.disableInteraction(dto)
        interaction.status = 'disabled'
        interaction.modifiedOnUtc = new Date()
        taskUserInteractionRepository.save(interaction)
        notifyChanged(interaction)
    }

    private void notifyChanged(TaskUserInteraction taskUserInteraction) {
        def interactionDto = UserInteractionMapper.toUserInteractionDto(taskUserInteraction)
        taskWebsocketProducer.changeUserInteraction(interactionDto)
    }

    private TaskUserInteraction findByDto(UserInteractionStateDto dto) {
        if (dto.interactionId) {
            return findById(dto.interactionId)
        }
        def option = taskUserInteractionRepository.findByTaskIdAndPageContext(dto.taskId, dto.pageContext)
        if(option.present) {
            return option.get()
        }
    }

}
