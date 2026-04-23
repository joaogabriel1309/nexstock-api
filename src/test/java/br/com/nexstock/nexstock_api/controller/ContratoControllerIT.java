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
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Empresa empresaNova;
    private Plano plano;
    private String token;
    private Empresa empresaAdmin;

    @BeforeEach
    void setUp() {
        limparBanco();

        plano = planoRepository.save(Plano.builder()
                .nome("Plano Premium")
                .preco(BigDecimal.valueOf(199.90))
                .duracaoDias(30)
                .maxDispositivos(10)
                .ativo(true)
                .build());

        empresaAdmin = empresaRepository.save(Empresa.builder()
                .nome("NexStock Admin LTDA")
                .razaoSocial("NexStock Admin LTDA razao")
                .cpfCnpj("00000000000100")
                .ativo(true)
                .build());

        contratoRepository.save(Contrato.builder()
                .empresa(empresaAdmin)
                .plano(plano)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(30))
                .build());

        usuarioRepository.save(Usuario.builder()
                .empresa(empresaAdmin)
                .nome("João Gabriel")
                .email("admin@nexstock.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        empresaNova = empresaRepository.save(Empresa.builder()
                .nome("Nova Empresa Teste")
                .razaoSocial("nova empresa razao social")
                .cpfCnpj("11111111111111")
                .ativo(true)
                .build());

        token = obterToken();
    }

    private String obterToken() {
        var body = Map.of(
                "email", "admin@nexstock.com",
                "senha", "admin123"
        );
        ResponseEntity<Map> r = restTemplate.postForEntity("/api/auth/login", body, Map.class);
        return r.getBody().get("token").toString();
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    @DisplayName("POST /api/contratos/{id}/renovar — gera novo contrato → 200")
    void renovar_contratoAtivo_retornaNovoContrato() {
        var bodyCriar = Map.of(
                "empresaId", empresaNova.getId().toString(),
                "planoId", plano.getId().toString()
        );
        ResponseEntity<ContratoResponse> criado = restTemplate.exchange(
                "/api/contratos", HttpMethod.POST,
                new HttpEntity<>(bodyCriar, headers()), ContratoResponse.class);

        UUID idOriginal = criado.getBody().getId();

        ResponseEntity<ContratoResponse> response = restTemplate.exchange(
                "/api/contratos/" + idOriginal + "/renovar", HttpMethod.POST,
                new HttpEntity<>(headers()), ContratoResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isNotEqualTo(idOriginal);
        assertThat(response.getBody().getRenovadoDeId()).isEqualTo(idOriginal);
    }
}
