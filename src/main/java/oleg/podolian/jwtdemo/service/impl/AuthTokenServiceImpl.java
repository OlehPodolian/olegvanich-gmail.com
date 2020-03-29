package oleg.podolian.jwtdemo.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oleg.podolian.jwtdemo.model.AuthTokenDTO;
import oleg.podolian.jwtdemo.service.AuthTokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private final static String TOKEN_USER_FIELD = "username";
    private final static String TOKEN_EXPIRATION_FIELD = "expires";
    private final static String JWT_SALT = "JWT_SECRET";
    private final static int TOKEN_EXPIRATION_MINUTES = 3600000;

    private final UserDetailsService userDetailsService;

    @Override
    public AuthTokenDTO generateToken(String username) {
        final Date expDate = getExpirationDateForOutcomingTokens();
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(TOKEN_USER_FIELD, username);
        tokenData.put(TOKEN_EXPIRATION_FIELD, expDate);

        final String tokenRaw = Jwts.builder()
                .setClaims(tokenData)
                .signWith(SignatureAlgorithm.HS512, JWT_SALT)
                .compact();

        String token = Base64.getEncoder().encodeToString(tokenRaw.getBytes(StandardCharsets.UTF_8));
        return new AuthTokenDTO("Bearer " + token, username, expDate);
    }

    @Override
    public UserDetails getUserByToken(String token) {

        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("MISSING_AUTHORIZATION_HEADER");
        }

        Claims claims = getClaims(token);
        String username = claims.get(TOKEN_USER_FIELD, String.class);

        Date expDate = claims.get(TOKEN_EXPIRATION_FIELD, Date.class);
        if (expDate.before(new Date())) {
            log.error("Invalid token lifetime ");
            throw new RuntimeException("EXPIRED_AUTHENTICATION_TOKEN");

        }
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public boolean isValid(String jwtToken, UserDetails userDetails) {
        Claims claims = getClaims(jwtToken);
        return new Date().before(new Date((Long) claims.get(TOKEN_EXPIRATION_FIELD)))
                && ((String) claims.get(TOKEN_USER_FIELD)).equalsIgnoreCase(userDetails.getUsername());
    }

    private Claims getClaims(String token) {
        String rawToken = new String(Base64.getDecoder().decode(token));
        JwtParser parser = Jwts.parser();
        Claims claims;
        try {
            parser.setSigningKey(JWT_SALT);
            Jws<Claims> jws = parser.parseClaimsJws(rawToken);
            claims = jws.getBody();
        } catch (Exception ex) {
            throw new RuntimeException("TOKEN_ERROR");
        }
        if (!claims.containsKey(TOKEN_USER_FIELD) || !claims.containsKey(TOKEN_EXPIRATION_FIELD)) {
            throw new RuntimeException("INVALID_AUTHENTICATION_TOKEN");
        }
        return claims;
    }

    private Date getExpirationDateForOutcomingTokens() {
        return new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MINUTES);
    }
}
