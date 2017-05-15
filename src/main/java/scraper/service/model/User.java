package scraper.service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class User {

	@Id
	public String id;

	public String email;

	public String password;

	public String salt;

	public Boolean isActive;
}
