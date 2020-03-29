package oleg.podolian.jwtdemo.repository.impl;

import oleg.podolian.jwtdemo.model.User;
import oleg.podolian.jwtdemo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final static Map<String, UserDetails> users = new ConcurrentHashMap<>(1);

    @PostConstruct
    public void init() {
        users.put("test@test.com", new User("test@test.com", "password", "USER"));
    }

    @Override
    public UserDetails findByUsername(String username) {
        return users.get(username);
    }

}
