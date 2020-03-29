package oleg.podolian.jwtdemo.service;

import oleg.podolian.jwtdemo.model.AuthTokenDTO;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;

public interface AuthTokenService {

    AuthTokenDTO generateToken(String username);

    UserDetails getUserByToken(String token);

    boolean isValid(String jwtToken, UserDetails userDetails);
}
