package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.expiracao-ms:86400000}")
    private long expiracaoMs;

    public String gerarToken(Usuario usuario) {
        Date agora     = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId",  usuario.getId().toString())
                .claim("empresaId",  usuario.getEmpresa().getId().toString())
                .claim("role",       usuario.getRole().name())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getChave())
                .compact();
    }

    public UUID extrairEmpresaId(String token) {
        String idStr = getClaims(token).get("empresaId", String.class);
        return UUID.fromString(idStr);
    }

    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    public UUID extrairUsuarioId(String token) {
        return UUID.fromString(getClaims(token).get("usuarioId", String.class));
    }

    public boolean isTokenValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token JWT inválido: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getChave() {
       return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}