package scraper.service.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import scraper.core.pipeline.PipelineBuilder;

@RestController
public class HelloController {

	@RequestMapping("/")
	public String index() {
		PipelineBuilder builder = new PipelineBuilder();
		return "Greetings from Spring Boot!";
	}

}
