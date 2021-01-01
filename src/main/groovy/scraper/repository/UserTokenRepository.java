package scraper.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.model.User;
import scraper.model.UserToken;

import java.util.List;

public interface UserTokenRepository extends MongoRepository<UserToken, String> {

	UserToken findByToken(@Param("token") String token);
	List<UserToken> findByUserOrderByCreatedOnDesc(User user);
}
