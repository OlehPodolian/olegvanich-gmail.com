package oleg.podolian.jwtdemo.service;

import oleg.podolian.jwtdemo.model.User;

public interface UserService {
    User findValidUser(User user);
}
