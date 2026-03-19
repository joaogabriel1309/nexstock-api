package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Produto Controller — Integração")
class ProdutoControllerIT extends IntegrationTestBase {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private DispositivoRepository dispositivoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Contrato contrato;
    private Dispositivo dispositivo;
    private String token;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        dispositivoRepository.deleteAll();
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

        dispositivo = dispositivoRepository.save(Dispositivo.builder()
                .contrato(contrato)
                .nome("Dispositivo Test")
                .sistema("Androi 13")
                .build());

        usuarioRepository.save(Usuario.builder()
                .contrato(contrato)
                .nome("Admin Test")
                .email("admin@test.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        token = obterToken("admin@test.com", "admin123");
    }

    private String obterToken(String email, String senha) {
        var body = Map.of(
                "email", email,
                "senha", senha,
                "contratoId", contrato.getId().toString()
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", body, Map.class);
        return response.getBody().get("token").toString();
    }

    private HttpHeaders headersComToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> produtoBody(String nome, String codigoBarras, double estoque) {
        return Map.of(
                "contratoId", contrato.getId().toString(),
                "dispositivoId", dispositivo.getId().toString(),
                "nome", nome,
                "codigoBarras", codigoBarras,
                "estoque", estoque
        );
    }

    @Test
    @DisplayName("POST /api/produtos — cria produto com sucesso → 201")
    void criar_dadosValidos_retorna201() {
        var body = produtoBody("Arroz 5kg", "7891234567890", 100.0);

        ResponseEntity<ProdutoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/produtos",
                HttpMethod.POST,
                new HttpEntity<>(body, headersComToken()),
                ProdutoResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNome()).isEqualTo("Arroz 5kg");
        assertThat(response.getBody().getEstoque()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(response.getBody().getVersao()).isEqualTo(1L);
        assertThat(response.getBody().getDeletado()).isFalse();
    }

    @Test
    @DisplayName("POST /api/produtos — código de barras duplicado → 422")
    void criar_codigoBarrasDuplicado_retornaErro() {
        var body = produtoBody("Arroz 5kg", "7891234567890", 100.0);

        restTemplate.exchange(baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(body, headersComToken()), ProdutoResponse.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/produtos",
                HttpMethod.POST,
                new HttpEntity<>(produtoBody("Feijão 1kg", "7891234567890", 50.0), headersComToken()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /api/produtos — sem autenticação → 401/403")
    void criar_semToken_retornaUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/produtos",
                HttpMethod.POST,
                new HttpEntity<>(produtoBody("Arroz", "123", 10.0), headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/produtos/contrato/{id} — lista produtos ativos → 200")
    void listar_produtosAtivos_retornaLista() {
        restTemplate.exchange(baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Arroz", "111", 10.0), headersComToken()), ProdutoResponse.class);
        restTemplate.exchange(baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Feijão", "222", 20.0), headersComToken()), ProdutoResponse.class);

        ResponseEntity<List<ProdutoResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headersComToken()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("GET /api/produtos/contrato/{cid}/{id} — produto existente → 200")
    void buscarPorId_existente_retorna200() {
        ResponseEntity<ProdutoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Arroz", "111", 10.0), headersComToken()),
                ProdutoResponse.class);

        UUID produtoId = criado.getBody().getId();

        ResponseEntity<ProdutoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId() + "/" + produtoId,
                HttpMethod.GET,
                new HttpEntity<>(headersComToken()),
                ProdutoResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(produtoId);
    }

    @Test
    @DisplayName("GET /api/produtos/contrato/{cid}/{id} — produto inexistente → 404")
    void buscarPorId_inexistente_retorna404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId() + "/" + UUID.randomUUID(),
                HttpMethod.GET,
                new HttpEntity<>(headersComToken()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/produtos/contrato/{cid}/{id} — atualiza produto → 200")
    void atualizar_dadosValidos_retorna200() {
        ResponseEntity<ProdutoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Arroz", "111", 10.0), headersComToken()),
                ProdutoResponse.class);

        UUID produtoId = criado.getBody().getId();
        var atualizacao = produtoBody("Arroz Premium", "111", 50.0);

        ResponseEntity<ProdutoResponse> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId() + "/" + produtoId,
                HttpMethod.PUT,
                new HttpEntity<>(atualizacao, headersComToken()),
                ProdutoResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getNome()).isEqualTo("Arroz Premium");
        assertThat(response.getBody().getEstoque()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(response.getBody().getVersao()).isEqualTo(2L);
    }

    @Test
    @DisplayName("DELETE /api/produtos/contrato/{cid}/{id} — soft delete → 204")
    void deletar_produtoExistente_retorna204() {
        ResponseEntity<ProdutoResponse> criado = restTemplate.exchange(
                baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Arroz", "111", 10.0), headersComToken()),
                ProdutoResponse.class);

        UUID produtoId = criado.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId() + "/" + produtoId
                        + "?dispositivoId=" + dispositivo.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headersComToken()),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List<ProdutoResponse>> lista = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headersComToken()),
                new ParameterizedTypeReference<>() {}
        );
        assertThat(lista.getBody()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/produtos/contrato/{id}?atualizadoDepois=... — sync incremental → retorna apenas novos")
    void sync_atualizadoDepois_retornaApenasProdutosNovos() throws InterruptedException {
        restTemplate.exchange(baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Produto Antigo", "111", 10.0), headersComToken()),
                ProdutoResponse.class);

        LocalDateTime corte = LocalDateTime.now();
        Thread.sleep(100);

        restTemplate.exchange(baseUrl() + "/api/produtos", HttpMethod.POST,
                new HttpEntity<>(produtoBody("Produto Novo", "222", 20.0), headersComToken()),
                ProdutoResponse.class);

        String desde = corte.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ResponseEntity<List<ProdutoResponse>> response = restTemplate.exchange(
                baseUrl() + "/api/produtos/contrato/" + contrato.getId()
                        + "?atualizadoDepois=" + desde,
                HttpMethod.GET,
                new HttpEntity<>(headersComToken()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNome()).isEqualTo("Produto Novo");
    }
}