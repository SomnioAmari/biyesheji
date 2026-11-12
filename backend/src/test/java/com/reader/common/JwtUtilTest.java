package com.reader.common;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET =
            "cHVyZS1yZWFkZXItanN3dC1zZWNyZXQta2V5LTIwMjUtMDUtMDEtMTIzNDU2Nzg5MA==";

    private JwtUtil newJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expireHours", 168L);
        return jwtUtil;
    }

    @Test
    void generateThenParseShouldReturnSameUserId() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken(42L, "alice");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    void parseInvalidTokenShouldThrow() {
        JwtUtil jwtUtil = newJwtUtil();
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> jwtUtil.parseUserId("invalid.token.value"));
    }
}
