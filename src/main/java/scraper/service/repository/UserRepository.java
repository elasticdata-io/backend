package scraper.service.repository;

import org.springframework.data.repository.Repository;
import scraper.service.model.User;

import java.util.List;

public interface UserRepository extends Repository<User, Long> {

	List<User> findByEmail(String user);
}
