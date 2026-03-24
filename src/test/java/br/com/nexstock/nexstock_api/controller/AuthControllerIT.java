package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Plano;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.repository.ContratoRepository;
import br.com.nexstock.nexstock_api.repository.PlanoRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth Controller — Integração")
class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Contrato contrato;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        contratoRepository.deleteAll();
        clienteRepository.deleteAll();
        planoRepository.deleteAll();

        Plano plano = planoRepository.save(Plano.builder()
                .nome("Plano Test")
                .preco(BigDecimal.valueOf(99.90))
                .duracaoDias(30)
                .maxDispositivos(5)
                .build());

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nome("Cliente Test")
                .email("cliente@test.com")
                .ativo(true)
                .build());

        contrato = contratoRepository.save(Contrato.builder()
                .cliente(cliente)
                .plano(plano)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .build());
    }

    @Test
    @DisplayName("Login com credenciais válidas retorna JWT")
    void login_credenciaisValidas_retornaJwt() {

        usuarioRepository.save(Usuario.builder()
                .contrato(contrato)
                .nome("João Test")
                .email("joao@test.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.ADMIN)
                .build());

        var body = Map.of("email", "joao@test.com", "senha", "senha123",
                "contratoId", contrato.getId().toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody().get("token").toString()).isNotBlank();
    }

    @Test
    @DisplayName("Login com senha errada retorna 401")
    void login_senhaErrada_retorna401() {
        usuarioRepository.save(Usuario.builder()
                .contrato(contrato)
                .nome("João Test")
                .email("joao@test.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.ADMIN)
                .build());

        var body = Map.of("email", "joao@test.com", "senha", "senhaerrada",
                "contratoId", contrato.getId().toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Login com email inexistente retorna 401")
    void login_emailInexistente_retorna401() {
        var body = Map.of("email", "naoexiste@test.com", "senha", "qualquer",
                "contratoId", contrato.getId().toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Registro de novo usuário por ADMIN retorna 201")
    void registro_adminValido_retorna201() {
        usuarioRepository.save(Usuario.builder()
                .contrato(contrato)
                .nome("Admin Test")
                .email("admin@test.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        var loginBody = Map.of("email", "admin@test.com", "senha", "admin123",
                "contratoId", contrato.getId().toString());

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", loginBody, Map.class);

        String token = loginResponse.getBody().get("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var novoUsuario = Map.of(
                "nome", "Novo Operador",
                "email", "operador@test.com",
                "senha", "operador123",
                "role", "OPERADOR",
                "contratoId", contrato.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/auth/registrar",
                HttpMethod.POST,
                new HttpEntity<>(novoUsuario, headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Registro sem autenticação retorna 403")
    void registro_semAutenticacao_retorna403() {
        var novoUsuario = Map.of(
                "nome", "Operador",
                "email", "operador@test.com",
                "senha", "operador123",
                "role", "OPERADOR",
                "contratoId", contrato.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/registrar", novoUsuario, Map.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }
}