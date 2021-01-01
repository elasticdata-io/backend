package scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));
	}
}