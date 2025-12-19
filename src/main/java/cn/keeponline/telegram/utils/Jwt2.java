package cn.keeponline.telegram.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author shaoshuai
 * @since 2018-06-13 15:00
 */
@Slf4j
@Component
public final class Jwt2 {

    /**
     * api JWT 过期时间 分钟
     */
    @Value("${project.jwtTimeout}")
    private int jwtTimeout;

    /**
     * JWT 发行方
     */
    @Value("${project.jwtIssuer}")
    private String jwtIssuer;

    /**
     * JWT 秘钥
     */
    @Value("${project.jwtSecret}")
    private String jwtSecret;

    public String encode(Map<String, String> customPayload) {
        JwtPayload payload = new JwtPayload();
        payload.setIssuer(this.jwtIssuer);
        payload.setExpiredAt(createJWTExpTime(this.jwtTimeout));
        payload.setCustomPayload(customPayload);
        return encode(payload);
    }

    private Date createJWTExpTime(long minute) {
        long current = System.currentTimeMillis();
        current += minute * 60 * 1000;
        return new Date(current);
    }

    public String encode(JwtPayload payload) {
        JWTCreator.Builder builder = JWT.create();
        if (!payload.getCustomPayload().isEmpty()) {
            payload.getCustomPayload().forEach(builder::withClaim);
        }
        ImmutableMap<String, Object> headerClaims = ImmutableMap.of(
                PublicClaims.TYPE, "JWT",
                PublicClaims.ALGORITHM, "HS256");
        return builder.withJWTId(payload.getJwtId())
                .withAudience(payload.getAudience())
                .withIssuedAt(new Date())
                .withExpiresAt(payload.getExpiredAt())
                .withSubject(payload.getSubject())
                .withHeader(headerClaims)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    @SuppressWarnings("unchecked")
    public JwtPayload decode(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            JwtPayload jwtPayload = new JwtPayload();
            jwtPayload.setIssuer(jwt.getIssuer());
            jwtPayload.setJwtId(jwt.getId());
            jwtPayload.setSubject(jwt.getSubject());
            jwtPayload.setExpiredAt(jwt.getExpiresAt());
            jwtPayload.setDecodePayload(jwt.getClaims());
            return jwtPayload;
        } catch (JWTVerificationException e) {
            log.error("Invalid signature/claims", e);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Jwt2() {
        super();
    }

    public static class JwtPayload {
        /**
         * 过期时间
         */
        private Date expiredAt;

        /**
         * 主题
         */
        private String subject;

        /**
         * 发行者
         */
        private String issuer;

        /**
         * JWT ID
         */
        private String jwtId;

        /**
         * 观众
         */
        private String audience;

        /**
         * 自定义payload
         */
        private Map<String, String> customPayload;

        /**
         * 解析后的payload
         */
        private Map<String, Claim> decodePayload;

        public Date getExpiredAt() {
            return expiredAt;
        }

        public void setExpiredAt(Date expiredAt) {
            this.expiredAt = expiredAt;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getJwtId() {
            return jwtId;
        }

        public void setJwtId(String jwtId) {
            this.jwtId = jwtId;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public void setCustomPayload(Map<String, String> customPayload) {
            this.customPayload = customPayload;
        }

        private Map<String, String> getCustomPayload() {
            return customPayload;
        }

        private void setDecodePayload(Map<String, Claim> decodePayload) {
            this.decodePayload = decodePayload;
        }

        public String getCustomPayloadValue(String name) {
            Claim claim = this.decodePayload.get(name);
            return Objects.nonNull(claim) ? claim.asString() : null;
        }
    }

}
