package com.amber.splitsmart.auth;

import com.amber.splitsmart.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Small, explicit JWT guard without a Spring Security filter chain. */
@Component
public class JwtRequestInterceptor implements HandlerInterceptor {
    private final JwtService jwt;
    private final UserRepository users;

    JwtRequestInterceptor(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "A bearer token is required");
            return false;
        }
        try {
            var user = users.findById(jwt.userId(header.substring(7))).orElse(null);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User no longer exists");
                return false;
            }
            request.setAttribute("currentUser", user);
            return true;
        } catch (RuntimeException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return false;
        }
    }
}
