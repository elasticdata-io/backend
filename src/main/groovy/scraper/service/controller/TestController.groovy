package scraper.service.controller

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.dto.model.task.PipelineRunDto

@RestController
@RequestMapping("/test")
class TestController {

    private static Logger logger = LogManager.getLogger(TestController.class)

    @PostMapping
    void test(@RequestBody(required=false) HashMap dto) {
        println dto
    }
}
