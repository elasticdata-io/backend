package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.UserToken;

public interface UserTokenRepository extends MongoRepository<UserToken, String> {

	UserToken findByToken(@Param("token") String token);
}
