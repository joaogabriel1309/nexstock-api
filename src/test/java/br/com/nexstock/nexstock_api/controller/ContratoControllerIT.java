package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
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

@DisplayName("Contrato Controller — Integração")
class ContratoControllerIT extends IntegrationTestBase {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Contrato contratoAdmin;
    private Cliente clienteNovo;
    private Plano plano;
    private String token;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        contratoRepository.deleteAll();
        clienteRepository.deleteAll();
        planoRepository.deleteAll();

        plano = planoRepository.save(Plano.builder()
                .nome("Plano Test")
                .preco(BigDecimal.valueOf(99.90))
                .duracaoDias(30)
                .maxDispositivos(5)
                .ativo(true)
                .build());

        Cliente clienteAdmin = clienteRepository.save(Cliente.builder()
                .nome("Admin Cliente")
                .email("admin-cliente@test.com")
                .ativo(true)
                .build());

        contratoAdmin = contratoRepository.save(Contrato.builder()
                .cliente(clienteAdmin)
                .plano(plano)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .build());

        usuarioRepository.save(Usuario.builder()
                .contrato(contratoAdmin)
                .nome("Admin Test")
                .email("admin@test.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        clienteNovo = clienteRepository.save(Cliente.builder()
                .nome("Cliente Novo")
                .email("novo@test.com")
                .ativo(true)
                .build());

        token = obterToken();
    }

    private String obterToken() {
        var body = Map.of("email", "admin@test.com", "senha", "admin123",
                "contratoId", contratoAdmin.getId().toString());
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
    @DisplayName("POST /api/contratos — cria contrato → 201")
    void contratar_dadosValidos_retorna201() {
        var body = Map.of(
                "clienteId", clienteNovo.getId().toString(),
                "planoId", plano.getId().toString()
        );

        ResponseEntity<ContratoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), ContratoResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusContrato.ATIVO);
        assertThat(response.getBody().getClienteId()).isEqualTo(clienteNovo.getId());
        assertThat(response.getBody().getDataFim()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    @DisplayName("POST /api/contratos — cliente já tem contrato ativo → 409")
    void contratar_clienteJaTemContratoAtivo_retorna409() {
        var body = Map.of(
                "clienteId", contratoAdmin.getCliente().getId().toString(),
                "planoId", plano.getId().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /api/contratos — plano inexistente → 404")
    void contratar_planoInexistente_retorna404() {
        var body = Map.of(
                "clienteId", clienteNovo.getId().toString(),
                "planoId", UUID.randomUUID().toString()
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/contratos/{id} — contrato existente → 200")
    void buscarPorId_existente_retorna200() {
        ResponseEntity<ContratoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + contratoAdmin.getId(), HttpMethod.GET,
                new HttpEntity<>(headers()), ContratoResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(contratoAdmin.getId());
    }

    @Test
    @DisplayName("GET /api/contratos/{id} — inexistente → 404")
    void buscarPorId_inexistente_retorna404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + UUID.randomUUID(), HttpMethod.GET,
                new HttpEntity<>(headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/contratos/cliente/{id} — lista contratos do cliente → 200")
    void listarPorCliente_retornaLista() {
        ResponseEntity<List<ContratoResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/contratos/cliente/" + contratoAdmin.getCliente().getId(),
                HttpMethod.GET, new HttpEntity<>(headers()), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("PATCH /api/contratos/{id}/cancelar — cancela → 204")
    void cancelar_contratoAtivo_retorna204() {
        var body = Map.of("clienteId", clienteNovo.getId().toString(),
                "planoId", plano.getId().toString());
        ResponseEntity<ContratoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), ContratoResponse.class);

        UUID id = criado.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id + "/cancelar", HttpMethod.PATCH,
                new HttpEntity<>(headers()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ContratoResponse> buscado = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ContratoResponse.class);

        assertThat(buscado.getBody().getStatus()).isEqualTo(StatusContrato.CANCELADO);
    }

    @Test
    @DisplayName("PATCH /api/contratos/{id}/suspender → 204 e PATCH /reativar → 204")
    void suspender_e_reativar_contrato() {
        var body = Map.of("clienteId", clienteNovo.getId().toString(),
                "planoId", plano.getId().toString());
        ResponseEntity<ContratoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), ContratoResponse.class);

        UUID id = criado.getBody().getId();

        ResponseEntity<Void> suspender = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id + "/suspender", HttpMethod.PATCH,
                new HttpEntity<>(headers()), Void.class);
        assertThat(suspender.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ContratoResponse> suspenso = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ContratoResponse.class);
        assertThat(suspenso.getBody().getStatus()).isEqualTo(StatusContrato.SUSPENSO);

        ResponseEntity<Void> reativar = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id + "/reativar", HttpMethod.PATCH,
                new HttpEntity<>(headers()), Void.class);
        assertThat(reativar.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ContratoResponse> reativado = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), ContratoResponse.class);
        assertThat(reativado.getBody().getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    @Test
    @DisplayName("POST /api/contratos/{id}/renovar — renova contrato → 200 com novo contrato")
    void renovar_contratoAtivo_retornaNovoContrato() {
        var body = Map.of("clienteId", clienteNovo.getId().toString(),
                "planoId", plano.getId().toString());
        ResponseEntity<ContratoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(body, headers()), ContratoResponse.class);

        UUID idOriginal = criado.getBody().getId();

        ResponseEntity<ContratoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/contratos/" + idOriginal + "/renovar", HttpMethod.POST,
                new HttpEntity<>(headers()), ContratoResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isNotEqualTo(idOriginal);
        assertThat(response.getBody().getRenovadoDeId()).isEqualTo(idOriginal);
        assertThat(response.getBody().getStatus()).isEqualTo(StatusContrato.ATIVO);
    }
}