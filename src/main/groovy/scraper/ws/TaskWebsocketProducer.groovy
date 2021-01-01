package scraper.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class TaskWebsocketProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(scraper.dto.model.task.TaskDto taskDto) {
        String channel = '/task/change/' + taskDto.userId
        messagingTemplate.convertAndSend(channel, taskDto)
    }

    void parsedLines(scraper.dto.model.pipeline.TaskParsedLinesDto pipelineParsedLinesDto) {
        String channel = '/task/parsed-lines/' + pipelineParsedLinesDto.userId
        messagingTemplate.convertAndSend(channel, pipelineParsedLinesDto)
    }

    void startCommandExecute(scraper.dto.model.task.command.TaskCommandExecuteDto pipelineCommandExecuteDto) {
        String channel = '/task/command/execute/' + pipelineCommandExecuteDto.userId
        messagingTemplate.convertAndSend(channel, pipelineCommandExecuteDto)
    }

    void changeUserInteraction(scraper.dto.model.task.UserInteractionDto userInteraction) {
        String channel = '/task/interaction/' + userInteraction.userId
        messagingTemplate.convertAndSend(channel, userInteraction)
    }

}
