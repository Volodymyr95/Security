package cursor.com.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import cursor.com.entity.User;
import cursor.com.service.UserService;
import cursor.com.service.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static cursor.com.configuration.SecurityConstants.*;

@RequiredArgsConstructor
public class JWTAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginDto user = null;
        try {
            user = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        var userDetails = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        User user = userService.getByUsername(userDetails.getUsername());

        String token = JWT.create()
            .withSubject(user.getUsername())
            .withClaim(USER_ID, user.getId())
            .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_DATE))
            .sign(HMAC512(SECRET.getBytes()));

        response.setHeader(TOKEN_HEADER, TOKEN_PREFIX + token);
        response.setContentType("application/json");

    }
}
