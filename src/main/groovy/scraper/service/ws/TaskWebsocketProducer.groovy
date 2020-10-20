package scraper.service.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.service.dto.model.task.UserInteractionDto
import scraper.service.dto.model.task.command.TaskCommandExecuteDto
import scraper.service.dto.model.pipeline.TaskParsedLinesDto
import scraper.service.dto.model.task.TaskDto

@Service
class TaskWebsocketProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(TaskDto taskDto) {
        String channel = '/task/change/' + taskDto.userId
        messagingTemplate.convertAndSend(channel, taskDto)
    }

    void parsedLines(TaskParsedLinesDto pipelineParsedLinesDto) {
        String channel = '/task/parsed-lines/' + pipelineParsedLinesDto.userId
        messagingTemplate.convertAndSend(channel, pipelineParsedLinesDto)
    }

    void startCommandExecute(TaskCommandExecuteDto pipelineCommandExecuteDto) {
        String channel = '/task/command/execute/' + pipelineCommandExecuteDto.userId
        messagingTemplate.convertAndSend(channel, pipelineCommandExecuteDto)
    }

    void changeUserInteraction(UserInteractionDto userInteraction) {
        String channel = '/task/interaction/' + userInteraction.userId
        messagingTemplate.convertAndSend(channel, userInteraction)
    }

}
