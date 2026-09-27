package com.amber.splitsmart.auth;
import com.amber.splitsmart.user.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
    private final JwtProperties properties;
    JwtService(JwtProperties properties) { this.properties = properties; }
    public String createToken(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getId().toString()).claim("email", user.getEmail()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(properties.expirationHours() * 3600))).signWith(key()).compact();
    }
    public Long userId(String token) { return Long.valueOf(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject()); }
    private SecretKey key() { return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)); }
}
