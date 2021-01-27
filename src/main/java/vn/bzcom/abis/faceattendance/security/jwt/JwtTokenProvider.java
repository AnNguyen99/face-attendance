package vn.bzcom.abis.faceattendance.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.bzcom.abis.faceattendance.security.jwt.JwtConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static vn.bzcom.abis.faceattendance.security.jwt.JwtConstants.SIGNING_KEY;
import static vn.bzcom.abis.faceattendance.security.jwt.JwtConstants.AUTHORITIES_KEY;

@Component
@Slf4j
public class JwtTokenProvider implements Serializable {

    // Retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        String username = getClaimFromToken(token, Claims::getSubject);
        log.debug("username from token: " + username);
        return username;
    }

    // Retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        Date expirateDate = getClaimFromToken(token, Claims::getExpiration);
        log.debug("expiration date from token : " + expirateDate.toString());
        return expirateDate;
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claminsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claminsResolver.apply(claims);
    }

    // for retrieving any information from token
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // generate token for user
    public String generateToken(Authentication authentication) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        log.debug("username: " + authentication.getName());
        log.debug("authorities: " + authorities);
        String jwtToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*1000))
                .compact();
        log.debug("Generated JWT Token: " + jwtToken);
        return jwtToken;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())
                    && !isTokenExpired(token));
    }

    public UsernamePasswordAuthenticationToken getAuthentication(final String token, final Authentication existingAuth, final UserDetails userDetails) {
        final JwtParser jwtParser = Jwts.parser().setSigningKey(SIGNING_KEY);

        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);

        final Claims claims = claimsJws.getBody();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}
