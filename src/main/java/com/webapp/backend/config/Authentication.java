package com.webapp.backend.config;

import com.webapp.backend.Exception.AuthorizationException;
import com.webapp.backend.model.User;
import com.webapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class Authentication {

    @Autowired
    UserService userService;


    public User authenticate(HttpHeaders headers) throws AuthorizationException {

        if (headers.getFirst("authorization") == null || headers.getFirst("authorization").isEmpty()) {
            throw new AuthorizationException("Header Authorization absent.");
        }

        String token = headers.getFirst("authorization").substring(6);

        if (token == null || token.isEmpty() || token.isBlank())
            new AuthorizationException("Token cannot be empty!");

        byte[] actualByte = Base64.getDecoder().decode(token);
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");

        User u = userService.login(credentials[0], credentials[1]);

        return u;
    }
}
