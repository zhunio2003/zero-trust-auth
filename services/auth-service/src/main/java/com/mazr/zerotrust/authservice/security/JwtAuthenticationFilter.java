package com.mazr.zerotrust.authservice.security;

import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import com.mazr.zerotrust.authservice.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtValidator jwtValidator;

    @Override
    protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {

            filterChain.doFilter(request, response);
            return;

        }

        try {
            Claims claims = jwtValidator.validateToken(token);

            if (tokenBlacklistService.isBlacklisted(claims.getId())) {

                throw new InvalidTokenException();

            }

            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (InvalidTokenException e) {

            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            log.debug("Token reject", e);
            return;

        } catch (DataAccessException e) {

            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service unavailable");
            log.error("Redis unavailable while checking token blacklist", e);
            return;

        }


        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }

        return header.substring(BEARER_PREFIX.length());
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }

}
