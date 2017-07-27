package scraper.service.controller

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.core.browser.provider.Phantom
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository

@RestController
@RequestMapping("/api/pipeline-task")
class PipelineTaskController {

    private static Class DEFAULT_BROWSER = Phantom.class;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;


    @Autowired
    AmqpTemplate rabbitTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping("/list/{pipelineId}")
    List<PipelineTask> list(@PathVariable String pipelineId) {
        return pipelineTaskRepository.findByPipelineOrderByEndOnDesc(pipelineId);
    }

    @RequestMapping("/data/{id}")
    List<HashMap> getData(@PathVariable String id) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOne(id);
        return pipelineTask.data;
    }

    @RequestMapping("/delete/{id}")
    void delete(@PathVariable String id) {
        pipelineTaskRepository.delete(id);
    }
}
