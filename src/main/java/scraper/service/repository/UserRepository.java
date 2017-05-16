package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.User;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

	List<User> findByEmail(@Param("email") String email);
}
