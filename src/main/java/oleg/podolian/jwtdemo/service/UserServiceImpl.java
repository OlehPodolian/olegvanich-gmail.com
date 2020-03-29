package oleg.podolian.jwtdemo.service;

import lombok.RequiredArgsConstructor;
import oleg.podolian.jwtdemo.model.User;
import oleg.podolian.jwtdemo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findValidUser(User user) {
        UserDetails found = userRepository.findByUsername(user.getUsername());
        if (found.getPassword().equals(user.getPassword())) {
            return (User) found;
        }
        throw new RuntimeException("USERNAME_PASSWORD_INVALID");
    }
}
