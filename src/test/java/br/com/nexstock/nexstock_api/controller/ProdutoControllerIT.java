package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.exception.ArquivoInvalidoException;
import br.com.nexstock.nexstock_api.exception.FalhaUploadException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import br.com.nexstock.nexstock_api.service.JwtService;
import br.com.nexstock.nexstock_api.storage.StorageService;
import br.com.nexstock.nexstock_api.storage.StorageUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ProdutoController - upload de imagem")
class ProdutoControllerIT extends IntegrationTestBase {

    @Autowired ProdutoRepository produtoRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @MockitoBean StorageService storageService;

    private Empresa empresa;
    private Produto produto;
    private String token;

    @BeforeEach
    void setUp() {
        limparBanco();

        empresa = empresaRepository.save(Empresa.builder()
                .nome("NexStock Test")
                .razaoSocial("NexStock Test LTDA")
                .cpfCnpj("12345678000199")
                .ativo(true)
                .build());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .nome("Admin Test")
                .email("admin-upload@nexstock.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .ativo(true)
                .build());

        produto = produtoRepository.save(Produto.builder()
                .empresa(empresa)
                .nome("Arroz 5kg")
                .sku("ARROZ-5KG")
                .codigoBarras("7891000000001")
                .descricao("Produto teste")
                .unidadeMedida("UN")
                .precoCusto(BigDecimal.valueOf(10))
                .precoVenda(BigDecimal.valueOf(15))
                .estoqueAtual(BigDecimal.TEN)
                .estoqueMinimo(BigDecimal.ONE)
                .ativo(true)
                .permiteVendaSemEstoque(false)
                .versao(1L)
                .build());

        token = jwtService.gerarToken(usuario);
        reset(storageService);
    }

    @Test
    @DisplayName("POST /api/produtos/{id}/imagem deve salvar imagem do produto")
    void uploadImagem_deveSalvarImagemDoProduto() throws Exception {
        var file = new MockMultipartFile(
                "arquivo", "produto.png", "image/png", "conteudo".getBytes());
        var upload = new StorageUploadResult(
                "produtos/%s/%s/produto.png".formatted(empresa.getId(), produto.getId()),
                "https://cdn.test.local/produtos/produto.png");

        when(storageService.uploadProductImage(
                any(), eq(empresa.getId().toString()), eq(produto.getId().toString())))
                .thenReturn(upload);

        mockMvc.perform(multipart("/api/produtos/{id}/imagem", produto.getId())
                        .file(file)
                        .param("empresaId", empresa.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(produto.getId().toString()))
                .andExpect(jsonPath("$.imagemUrl").value(upload.url()))
                .andExpect(jsonPath("$.imagemKey").value(upload.key()));

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertThat(atualizado.getImagemUrl()).isEqualTo(upload.url());
        assertThat(atualizado.getImagemKey()).isEqualTo(upload.key());
        assertThat(atualizado.getVersao()).isEqualTo(2L);
    }

    @Test
    @DisplayName("POST /api/produtos/{id}/imagem deve retornar 404 quando produto nao existe")
    void uploadImagem_produtoInexistente_deveRetornar404() throws Exception {
        var file = new MockMultipartFile(
                "arquivo", "produto.png", "image/png", "conteudo".getBytes());

        mockMvc.perform(multipart("/api/produtos/{id}/imagem", java.util.UUID.randomUUID())
                        .file(file)
                        .param("empresaId", empresa.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("POST /api/produtos/{id}/imagem deve retornar 400 para arquivo invalido")
    void uploadImagem_arquivoInvalido_deveRetornar400() throws Exception {
        var file = new MockMultipartFile(
                "arquivo", "produto.gif", "image/gif", "gif".getBytes());

        when(storageService.uploadProductImage(any(), any(), any()))
                .thenThrow(new ArquivoInvalidoException("Tipo de arquivo nao permitido."));

        mockMvc.perform(multipart("/api/produtos/{id}/imagem", produto.getId())
                        .file(file)
                        .param("empresaId", empresa.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Arquivo invalido"));
    }

    @Test
    @DisplayName("POST /api/produtos/{id}/imagem deve retornar 502 quando storage falha")
    void uploadImagem_falhaStorage_deveRetornar502() throws Exception {
        var file = new MockMultipartFile(
                "arquivo", "produto.png", "image/png", "conteudo".getBytes());

        when(storageService.uploadProductImage(any(), any(), any()))
                .thenThrow(new FalhaUploadException("Falha ao enviar imagem para o storage.", new RuntimeException()));

        mockMvc.perform(multipart("/api/produtos/{id}/imagem", produto.getId())
                        .file(file)
                        .param("empresaId", empresa.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("Falha no upload"));
    }

    @Test
    @DisplayName("POST /api/produtos/{id}/imagem deve exigir autenticacao")
    void uploadImagem_semToken_deveRejeitar() throws Exception {
        var file = new MockMultipartFile(
                "arquivo", "produto.png", "image/png", "conteudo".getBytes());

        mockMvc.perform(multipart("/api/produtos/{id}/imagem", produto.getId())
                        .file(file)
                        .param("empresaId", empresa.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());

        verifyNoInteractions(storageService);
    }
}
