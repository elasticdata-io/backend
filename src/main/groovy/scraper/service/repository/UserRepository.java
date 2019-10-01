package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findById(String id);
	User findByLogin(@Param("login") String login);
	User findByEmail(@Param("email") String email);
}
