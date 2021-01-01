package scraper.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.dto.model.pipeline.TaskParsedLinesDto
import scraper.dto.model.task.TaskDto
import scraper.dto.model.task.UserInteractionDto
import scraper.dto.model.task.command.TaskCommandExecuteDto

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
