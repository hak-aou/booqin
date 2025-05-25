package fr.uge.booqin.infra.security.auth.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fr.uge.booqin.infra.security.exception.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

/**
 * Utility class for JWT operations
 */
@Component
public class JwtUtil {

    public final SecretKey secret;

    public JwtUtil(SecretKeySpec secretKeySpec) {
        this.secret =  secretKeySpec;
    }

    /**
     * Generates a new OAuthInfo object for a given subject
     * @throws JwtException if tokens could not be generated
     */
    public JwtData generateTokens(String subject, Duration accessExpiration, Duration refreshExpiration) {
        return new JwtData(
                generateToken(subject, expirationIn(accessExpiration)),
                generateToken(subject, expirationIn(refreshExpiration)));
    }

    /**
     * Generates a new access token for a given subject
     * @return the generated access token
     * @throws JwtException if the token could not be generated
     */
    public String refreshToken(String subject, Duration accessExpiration)  {
        return generateToken(subject, expirationIn(accessExpiration));
    }

    /**
     * Generates a new token for a given subject and expiration time
     * @param expirationTime the expiration time of the token
     * @return the generated token
     * @throws JwtException if the token could not be generated
     */
    public String generateToken(String subject, Date expirationTime) {
        try {
            var signer = new MACSigner(secret);
            var claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .expirationTime(expirationTime)
                    .issueTime(new Date())
                    .build();
            var signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw JwtException.processingError();
        }
    }

    /**
     * Validates a token and returns the subject (userId) if the token is valid
     * @param token the token to validate
     * @return the subject (user userId)
     * @throws AuthenticationException if the token is invalid
     * @throws JwtException if the token could not be generated
     */
    public String validateAndGetSubject(String token) throws AuthenticationException {
        JWTClaimsSet claims;
        try {
            var signedJWT = SignedJWT.parse(token);
            var verifier = new MACVerifier(secret);
            if (!signedJWT.verify(verifier)) {
                throw new AuthenticationException("Invalid token");
            }
            claims = signedJWT.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw JwtException.processingError();
        }
        var expirationTime = claims.getExpirationTime();
        if (expirationTime != null && new Date().after(expirationTime)) {
            throw new AuthenticationException("Token expired");
        }
        return claims.getSubject();
    }


    public Date expirationIn(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.toMillis());
    }
}