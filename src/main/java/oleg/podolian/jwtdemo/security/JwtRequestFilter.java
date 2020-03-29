package oleg.podolian.jwtdemo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import oleg.podolian.jwtdemo.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

@Log4j2
public class JwtRequestFilter extends AbstractAuthenticationProcessingFilter {

    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_HEADER_PREFIX = "Bearer ";

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDetailsService userDetailsService;


    public JwtRequestFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setAuthenticationSuccessHandler(((request, response, authentication) -> {
            String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
            request.getRequestDispatcher(request.getServletPath() + pathInfo).forward(request, response);
        }));
        setAuthenticationFailureHandler(((request, response, e) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            String responseJSON = objectMapper.writeValueAsString(Collections.singletonMap("CAUSE", "INVALID_AUTHENTICATION_TOKEN"));
            response.getWriter().write(responseJSON);
        }));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        final String requestTokenHeader = request.getHeader(AUTH_HEADER);
        String jwtToken = null;
        String username = null;

        if (Objects.nonNull(requestTokenHeader)
                && requestTokenHeader.startsWith(AUTH_HEADER_PREFIX)) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = authTokenService.getUserByToken(jwtToken).getUsername();
            } catch (IllegalArgumentException e) {
                log.error("Unable to get JWT token");
            }
        } else {
            String message = String.format("JWT Token is missing or does not start with %s prefix", AUTH_HEADER_PREFIX);
            logger.warn(message);
            throw new RuntimeException("MISSING_AUTHORIZATION_HEADER: " + message);
        }

        if (Objects.nonNull(username)
                && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (authTokenService.isValid(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                return authenticationToken;
            }
        }
        return null;
    }

    @Override
    protected AuthenticationManager getAuthenticationManager() {
        return super.getAuthenticationManager();
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}
