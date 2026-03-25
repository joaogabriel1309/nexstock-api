package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
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
    private UUID empresaId;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secret = Base64.getEncoder().encodeToString("minha-secret-super-segura-nexstock-2026".getBytes());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiracaoMs", 86400000L);

        empresaId = UUID.randomUUID();
        Empresa empresa = Empresa.builder().id(empresaId).nome("NexStock Cuiabá").build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .empresa(empresa)
                .email("joao@nexstock.com")
                .senha("$2a$hash")
                .role(Role.ADMIN)
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
    @DisplayName("deve extrair empresaId do token")
    void deveExtrairEmpresaId() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairEmpresaId(token)).isEqualTo(empresaId);
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
        String adulterado = token.substring(0, token.length() - 5) + "ABCDE";

        assertThat(jwtService.isTokenValido(adulterado)).isFalse();
    }

    @Test
    @DisplayName("deve rejeitar token expirado")
    void deveRejeitarTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expiracaoMs", -1000L);
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.isTokenValido(token)).isFalse();
    }
}