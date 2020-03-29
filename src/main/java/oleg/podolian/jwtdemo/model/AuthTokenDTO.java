package oleg.podolian.jwtdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDTO {

    private String token;
    private String username;
    private String userRole;
    private long expiresAt;

    public AuthTokenDTO(String token, String username, Date expDate) {
        this.token = token;
        this.username = username;
        this.expiresAt = expDate.getTime();
    }
}
