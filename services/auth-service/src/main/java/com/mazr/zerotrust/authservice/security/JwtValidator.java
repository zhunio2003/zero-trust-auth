package com.mazr.zerotrust.authservice.security;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

    private final JwtParser jwtParser;

    public JwtValidator(RsaKeyProperties rsaKeyProperties) {
        this.jwtParser = Jwts.parser()
            .verifyWith(rsaKeyProperties.getPublicKey())
            .sig()
                .add(Jwts.SIG.RS256)
            .and()
            .build();
    }

    public Claims validateToken(String jwtToken) {

        try {
            Claims claims = jwtParser.parseSignedClaims(jwtToken).getPayload();
            requirePresent(claims.getSubject());
            requirePresent(claims.getExpiration());
            requirePresent(claims.getIssuedAt());
            requirePresent(claims.get("role", String.class));
            requirePresent(claims.get("department", String.class));
            requirePresent(claims.getId());
            return claims;
        }  catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }

    }

    private void requirePresent(Object claimValue) {
        if (claimValue == null || claimValue.toString().isBlank()) {
            throw new InvalidTokenException();
        }
    }
}
