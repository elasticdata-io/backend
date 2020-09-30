package scraper.service.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.service.dto.model.pipeline.PipelineDto

@Service
class PipelineWebsocketProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(PipelineDto pipelineDto) {
        String channel = '/pipeline/change/' + pipelineDto.userId
        messagingTemplate.convertAndSend(channel, pipelineDto)
    }
}
