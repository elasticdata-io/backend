package scraper.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class PipelineWebsocketProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(scraper.dto.model.pipeline.PipelineDto pipelineDto) {
        String channel = '/pipeline/change/' + pipelineDto.userId
        messagingTemplate.convertAndSend(channel, pipelineDto)
    }
}
