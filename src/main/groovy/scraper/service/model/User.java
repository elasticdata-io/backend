package scraper.service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class User {

	@Id
	public String id;

	public String login;

	public String email;

	public String firstName;

	public String secondName;

	public String lastName;

	public String password;

	public String salt;

	public String picture;

	public String googleUserId;
	public String facebookUserId;

	public Boolean isActive;

	public Boolean isAdmin;
}
