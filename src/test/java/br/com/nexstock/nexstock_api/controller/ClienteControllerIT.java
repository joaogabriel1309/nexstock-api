package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.response.ClienteResponse;
import br.com.nexstock.nexstock_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente Controller — Integração")
class ClienteControllerIT extends IntegrationTestBase {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Contrato contrato;
    private String token;

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
                .nome("Cliente Base")
                .email("base@test.com")
                .ativo(true)
                .build());

        contrato = contratoRepository.save(Contrato.builder()
                .cliente(cliente)
                .plano(plano)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .build());

        usuarioRepository.save(Usuario.builder()
                .contrato(contrato)
                .nome("Admin Test")
                .email("admin@test.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        token = obterToken();
    }

    private String obterToken() {
        var body = Map.of("email", "admin@test.com", "senha", "admin123",
                "contratoId", contrato.getId().toString());
        ResponseEntity<Map> r = restTemplate.postForEntity(baseUrl() + "/api/auth/login", body, Map.class);
        return r.getBody().get("token").toString();
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    @DisplayName("POST /api/clientes — cadastra cliente → 201")
    void cadastrar_dadosValidos_retorna201() {
        var body = Map.of("nome", "João Silva", "email", "joao@empresa.com");

        ResponseEntity<ClienteResponse> response = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(body, headers()), ClienteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getNome()).isEqualTo("João Silva");
        assertThat(response.getBody().getAtivo()).isTrue();
    }

    @Test
    @DisplayName("POST /api/clientes — email duplicado → 409")
    void cadastrar_emailDuplicado_retorna409() {
        var body = Map.of("nome", "João Silva", "email", "base@test.com");

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(body, headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /api/clientes — campos obrigatórios ausentes → 400")
    void cadastrar_semCamposObrigatorios_retorna400() {
        var body = Map.of("nome", "Sem Email");

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(body, headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/clientes/{id} — cliente existente → 200")
    void buscarPorId_existente_retorna200() {
        var criado = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "Maria", "email", "maria@test.com"), headers()),
                ClienteResponse.class);

        UUID id = criado.getBody().getId();

        ResponseEntity<ClienteResponse> response = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ClienteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("GET /api/clientes/{id} — inexistente → 404")
    void buscarPorId_inexistente_retorna404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + UUID.randomUUID(), HttpMethod.GET,
                new HttpEntity<>(headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/clientes — lista todos → 200")
    void listarTodos_retornaLista() {
        restTemplate.exchange(baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "C1", "email", "c1@test.com"), headers()), ClienteResponse.class);
        restTemplate.exchange(baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "C2", "email", "c2@test.com"), headers()), ClienteResponse.class);

        ResponseEntity<List<ClienteResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.GET,
                new HttpEntity<>(headers()), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} — atualiza → 200")
    void atualizar_dadosValidos_retorna200() {
        var criado = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "Original", "email", "original@test.com"), headers()),
                ClienteResponse.class);

        UUID id = criado.getBody().getId();
        var atualizacao = Map.of("nome", "Atualizado", "email", "atualizado@test.com");

        ResponseEntity<ClienteResponse> response = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id, HttpMethod.PUT,
                new HttpEntity<>(atualizacao, headers()), ClienteResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getNome()).isEqualTo("Atualizado");
    }

    @Test
    @DisplayName("PATCH /api/clientes/{id}/desativar — desativa → 204")
    void desativar_clienteAtivo_retorna204() {
        var criado = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "Para Desativar", "email", "desativar@test.com"), headers()),
                ClienteResponse.class);

        UUID id = criado.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id + "/desativar", HttpMethod.PATCH,
                new HttpEntity<>(headers()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ClienteResponse> buscado = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ClienteResponse.class);

        assertThat(buscado.getBody().getAtivo()).isFalse();
    }

    @Test
    @DisplayName("PATCH /api/clientes/{id}/reativar — reativa → 204")
    void reativar_clienteInativo_retorna204() {
        var criado = restTemplate.exchange(
                baseUrl() + "/api/clientes", HttpMethod.POST,
                new HttpEntity<>(Map.of("nome", "Para Reativar", "email", "reativar@test.com"), headers()),
                ClienteResponse.class);

        UUID id = criado.getBody().getId();

        restTemplate.exchange(baseUrl() + "/api/clientes/" + id + "/desativar",
                HttpMethod.PATCH, new HttpEntity<>(headers()), Void.class);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id + "/reativar", HttpMethod.PATCH,
                new HttpEntity<>(headers()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ClienteResponse> buscado = restTemplate.exchange(
                baseUrl() + "/api/clientes/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ClienteResponse.class);

        assertThat(buscado.getBody().getAtivo()).isTrue();
    }
}