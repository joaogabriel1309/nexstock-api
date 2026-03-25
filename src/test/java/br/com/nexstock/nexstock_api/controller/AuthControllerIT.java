package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth Controller — Integração")
class AuthControllerIT extends IntegrationTestBase {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAllInBatch();
        empresaRepository.deleteAllInBatch();
        contratoRepository.deleteAllInBatch();
        planoRepository.deleteAllInBatch();

        Plano plano = planoRepository.save(Plano.builder()
                .nome("Plano Test")
                .preco(BigDecimal.valueOf(99.90))
                .duracaoDias(30)
                .ativo(true)
                .build());

        Contrato contrato = contratoRepository.save(Contrato.builder()
                .plano(plano)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .build());

        empresa = empresaRepository.save(Empresa.builder()
                .nome("NexStock Enterprise")
                .razaoSocial("NexStock Enterprise LTDA")
                .cpfCnpj("12345678000199")
                .contrato(contrato)
                .ativo(true)
                .build());
    }

    @Test
    @DisplayName("Login com email inexistente deve retornar 401")
    void login_emailInexistente_retorna401() {

        var body = Map.of(
                "email", "nao_existo@nexstock.com",
                "senha", "qualquer_senha"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Login com senha errada para o usuário deve retornar 401")
    void login_senha_incorreta_retorna401() {

        usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("joao gabriel")
                .email("joao@nexstock.com")
                .senha(passwordEncoder.encode("senha123"))
                .ativo(true)
                .role(Role.ADMIN)
                .build());

        var body = Map.of(
                "email", "joao@nexstock.com",
                "senha", "SENHA_ERRADA"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Registro de novo operador por ADMIN retorna 200")
    void registro_adminValido_retorna200() {
        usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("joao nexstock")
                .email("admin@nexstock.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        var loginBody = Map.of(
                "email", "admin@nexstock.com",
                "senha", "admin123",
                "empresaId", empresa.getId().toString()
        );
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginBody, Map.class);

        String token = loginResponse.getBody().get("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var novoUsuario = Map.of(
                "nome", "Operador de Caixa",
                "email", "caixa1@nexstock.com",
                "senha", "caixa123",
                "role", "OPERADOR",
                "empresaId", empresa.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/registrar",
                HttpMethod.POST,
                new HttpEntity<>(novoUsuario, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Registro com empresaId inexistente deve retornar 409")
    void registro_empresaInexistente_retorna409() {
        String token = obterTokenAdmin();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var novoUsuario = Map.of(
                "nome", "Sem Empresa",
                "email", "sem_empresa@nexstock.com",
                "senha", "senha123",
                "empresaId", UUID.randomUUID().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/registrar",
                HttpMethod.POST,
                new HttpEntity<>(novoUsuario, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Login com e-mail inválido deve retornar 400")
    void login_emailInvalido_retorna400() {
        var body = Map.of(
                "email", "email-invalido-sem-arroba",
                "senha", "123456"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Operador tentando registrar novo usuário deve retornar 403")
    void registro_porOperador_retorna403() {
        usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("Caixa 01")
                .email("caixa@nexstock.com")
                .senha(passwordEncoder.encode("123"))
                .role(Role.OPERADOR)
                .ativo(true)
                .build());

        var loginBody = Map.of("email", "caixa@nexstock.com", "senha", "123");
        String tokenOperador = restTemplate.postForEntity("/api/auth/login", loginBody, Map.class)
                .getBody().get("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenOperador);

        var novoUsuario = Map.of(
                "nome", "Outro Operador",
                "email", "outro@nexstock.com",
                "senha", "123",
                "empresaId", empresa.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/registrar",
                HttpMethod.POST,
                new HttpEntity<>(novoUsuario, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Acesso a endpoint protegido sem token deve retornar 401")
    void acesso_semToken_retorna401() {
        var novoUsuario = Map.of("nome", "Invasor", "email", "invasor@nexstock.com", "senha", "123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/registrar", novoUsuario, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Registro com Role inexistente deve retornar 400")
    void registro_roleInexistente_retorna400() {
        String token = obterTokenAdmin();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var corpoInvalido = Map.of(
                "nome", "Usuario Errado",
                "email", "errado@nexstock.com",
                "senha", "123",
                "role", "ROLE_QUE_NAO_EXISTE",
                "empresaId", empresa.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/registrar",
                HttpMethod.POST,
                new HttpEntity<>(corpoInvalido, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Login deve retornar payload completo para o Frontend")
    void login_retornaPayloadCompleto() {
        usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("João Gabriel")
                .email("joao@nexstock.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        var body = Map.of("email", "joao@nexstock.com", "senha", "senha123");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys(
                "token", "tipo", "usuarioId", "empresaId", "nome", "email", "role", "expiracaoEmMs"
        );
        assertThat(response.getBody().get("nome")).isEqualTo("João Gabriel");
        assertThat(response.getBody().get("empresaId")).isEqualTo(empresa.getId().toString());
    }

    private String obterTokenAdmin() {
        usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("Admin Test")
                .email("admin_test@nexstock.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        var loginBody = Map.of("email", "admin_test@nexstock.com", "senha", "admin123");
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/auth/login", loginBody, Map.class);
        return response.getBody().get("token").toString();
    }
}