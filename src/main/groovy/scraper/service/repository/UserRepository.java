package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.User;

public interface UserRepository extends MongoRepository<User, String> {

	User findByLogin(@Param("login") String login);
	User findByToken(@Param("token") String token);
}
