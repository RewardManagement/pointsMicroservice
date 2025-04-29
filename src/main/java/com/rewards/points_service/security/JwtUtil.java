package com.rewards.points_service.security;
 
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
import javax.crypto.SecretKey; // ✅ Correct import
 
import java.util.Date;
import java.util.function.Function;
 
@Component
public class JwtUtil {
 
    @Value("${jwt.secret}")
    private String secretKeyBase64;
 
    private SecretKey secretKey; // ✅ Use SecretKey, not Key
 
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes); // ✅ Automatically SecretKey
    }
 
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
 
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
 
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
 
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey) // ✅ Now correct
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
 
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
 
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}