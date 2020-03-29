package oleg.podolian.jwtdemo.repository;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository {

    UserDetails findByUsername(String username);
}
