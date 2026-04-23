package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import br.com.nexstock.nexstock_api.storage.StorageService;
import br.com.nexstock.nexstock_api.storage.StorageUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceTest {

    @Mock ProdutoRepository produtoRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock DispositivoService dispositivoService;
    @Mock StorageService storageService;

    @InjectMocks ProdutoService produtoService;

    private UUID empresaId;
    private UUID produtoId;
    private Empresa empresa;
    private Produto produto;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        produtoId = UUID.randomUUID();
        empresa = Empresa.builder()
                .id(empresaId)
                .nome("NexStock")
                .razaoSocial("NexStock LTDA")
                .cpfCnpj("12345678000199")
                .ativo(true)
                .build();

        produto = Produto.builder()
                .id(produtoId)
                .empresa(empresa)
                .nome("Arroz 5kg")
                .codigoBarras("7891000000001")
                .estoque(BigDecimal.TEN)
                .versao(1L)
                .build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve criar produto quando codigo de barras nao existe")
        void deveCriarProdutoComSucesso() {
            // Arrange
            var request = ProdutoRequest.builder()
                    .empresaId(empresaId)
                    .nome("Feijao 1kg")
                    .codigoBarras("7891000000002")
                    .estoque(BigDecimal.valueOf(20))
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(
                    request.getCodigoBarras(), empresaId)).thenReturn(false);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
                Produto salvo = invocation.getArgument(0);
                salvo.setId(UUID.randomUUID());
                return salvo;
            });

            // Act
            var response = produtoService.criar(request);

            // Assert
            assertThat(response.getNome()).isEqualTo("Feijao 1kg");
            assertThat(response.getEmpresaId()).isEqualTo(empresaId);
            verify(produtoRepository).save(any(Produto.class));
        }

        @Test
        @DisplayName("deve bloquear codigo de barras duplicado na empresa")
        void deveBloquearCodigoBarrasDuplicado() {
            // Arrange
            var request = ProdutoRequest.builder()
                    .empresaId(empresaId)
                    .nome("Produto duplicado")
                    .codigoBarras("7891000000001")
                    .estoque(BigDecimal.ONE)
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(
                    request.getCodigoBarras(), empresaId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> produtoService.criar(request))
                    .isInstanceOf(RegraDeNegocioException.class);
            verify(produtoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("uploadImagem")
    class UploadImagem {

        @Test
        @DisplayName("deve enviar imagem e persistir url e chave no produto")
        void deveEnviarImagemEPersistirUrlEChave() {
            // Arrange
            var arquivo = new MockMultipartFile(
                    "arquivo", "produto.png", "image/png", "conteudo".getBytes());
            var upload = new StorageUploadResult(
                    "produtos/%s/%s/produto.png".formatted(empresaId, produtoId),
                    "https://cdn.test.local/produtos/produto.png");

            when(produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId))
                    .thenReturn(Optional.of(produto));
            when(storageService.uploadProductImage(arquivo, empresaId.toString(), produtoId.toString()))
                    .thenReturn(upload);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var response = produtoService.uploadImagem(empresaId, produtoId, arquivo);

            // Assert
            assertThat(response.produtoId()).isEqualTo(produtoId);
            assertThat(response.imagemUrl()).isEqualTo(upload.url());
            assertThat(response.imagemKey()).isEqualTo(upload.key());
            assertThat(produto.getVersao()).isEqualTo(2L);
            verify(produtoRepository).save(produto);
        }

        @Test
        @DisplayName("deve falhar quando produto nao existe")
        void deveFalharQuandoProdutoNaoExiste() {
            // Arrange
            var arquivo = new MockMultipartFile(
                    "arquivo", "produto.png", "image/png", "conteudo".getBytes());
            when(produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> produtoService.uploadImagem(empresaId, produtoId, arquivo))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            verifyNoInteractions(storageService);
            verify(produtoRepository, never()).save(any());
        }
    }
}
