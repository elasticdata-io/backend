package scraper.service.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.service.model.Pipeline
import scraper.service.model.PipelineStatus
import scraper.service.model.User
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.repository.UserRepository

@RestController
@RequestMapping("/pipeline")
class PipelineDataController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

    @Autowired
    PipelineStatusRepository pipelineStatusRepository;

    @RequestMapping('/{id}')
    Pipeline get(@PathVariable String id) {
        return pipelineRepository.findOne(id);
    }

    @RequestMapping('/list')
    List<Pipeline> list(@RequestHeader("token") String token) {
        User user = userRepository.findByToken(token);
        return pipelineRepository.findByUser(user);
    }

    @RequestMapping('/save')
    Pipeline add(@RequestHeader String token, @RequestParam String id, @RequestParam String key,
                 @RequestParam String browser, @RequestParam String description, @RequestParam String jsonCommands) {
        User user = userRepository.findByToken(token);
        Pipeline pipeline = pipelineRepository.findOne(id);
        if (pipeline) {
            pipeline.browser = browser;
            pipeline.key = key;
            pipeline.modifiedOn = new Date();
            pipeline.description = description;
            pipeline.jsonCommands = jsonCommands;
            pipelineRepository.save(pipeline);
            return pipeline;
        }
        PipelineStatus status = pipelineStatusRepository.findByTitle('not running');
        pipeline = new Pipeline(key: key, browser: browser, jsonCommands: jsonCommands,
                user: user, description: description, createdOn: new Date(), modifiedOn: new Date(), status: status);
        pipelineRepository.save(pipeline);
        return pipeline;
    }
}
