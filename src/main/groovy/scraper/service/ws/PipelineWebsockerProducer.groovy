package scraper.service.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.service.dto.model.pipeline.PipelineCommandExecuteDto
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.dto.model.pipeline.PipelineParsedLinesDto

@Service
class PipelineWebsockerProducer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    void change(PipelineDto pipelineDto) {
        String channel = '/pipeline/change/' + pipelineDto.userId
        messagingTemplate.convertAndSend(channel, pipelineDto)
    }

    void parsedLines(PipelineParsedLinesDto pipelineParsedLinesDto) {
        String channel = '/pipeline/parsed-lines/' + pipelineParsedLinesDto.userId
        messagingTemplate.convertAndSend(channel, pipelineParsedLinesDto)
    }

    void commandExecute(PipelineCommandExecuteDto pipelineCommandExecuteDto) {
        String channel = '/pipeline/command/execute/' + pipelineCommandExecuteDto.userId
        messagingTemplate.convertAndSend(channel, pipelineCommandExecuteDto)
    }

}
