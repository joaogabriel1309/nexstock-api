package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;
    private UUID contratoId;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secret = Base64.getEncoder().encodeToString(new byte[64]);
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiracaoMs", 86400000L);

        contratoId = UUID.randomUUID();
        Contrato contrato = Contrato.builder().id(contratoId).build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .contrato(contrato)
                .email("joao@nexstock.com")
                .senha("$2a$hash")
                .role(Role.OPERADOR)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("deve gerar token válido e extrair email corretamente")
    void deveGerarEExtrairEmail() {
        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("joao@nexstock.com");
    }

    @Test
    @DisplayName("deve extrair contratoId do token")
    void deveExtrairContratoId() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairContratoId(token)).isEqualTo(contratoId);
    }

    @Test
    @DisplayName("deve validar token gerado como válido")
    void deveValidarTokenGerado() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.isTokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("deve rejeitar token adulterado")
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken(usuario);
        String adulterado = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isTokenValido(adulterado)).isFalse();
    }

    @Test
    @DisplayName("deve rejeitar token expirado")
    void deveRejeitarTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expiracaoMs", -1L);
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.isTokenValido(token)).isFalse();
    }
}