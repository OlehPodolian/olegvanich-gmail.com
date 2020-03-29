package oleg.podolian.jwtdemo.controller;

import lombok.RequiredArgsConstructor;
import oleg.podolian.jwtdemo.model.AuthTokenDTO;
import oleg.podolian.jwtdemo.model.User;
import oleg.podolian.jwtdemo.service.AuthTokenService;
import oleg.podolian.jwtdemo.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;
    private final AuthTokenService authTokenService;


    @PostMapping(value = "/authorize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthTokenDTO> authorize(@RequestBody User user) {
        User validUser = userService.findValidUser(user);
        AuthTokenDTO authTokenDTO = authTokenService.generateToken(validUser.getUsername());
        authTokenDTO.setUserRole(validUser.getRole());
        return ResponseEntity.ok(authTokenDTO);
    }
}
