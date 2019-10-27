package scraper.service.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.dto.model.pipeline.TaskCommandExecuteDto
import scraper.service.dto.model.pipeline.TaskParsedLinesDto
import scraper.service.dto.model.task.TaskDto

@Service
class PipelineWebsocketProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(PipelineDto pipelineDto) {
        String channel = '/pipeline/change/' + pipelineDto.userId
        messagingTemplate.convertAndSend(channel, pipelineDto)
    }
}
