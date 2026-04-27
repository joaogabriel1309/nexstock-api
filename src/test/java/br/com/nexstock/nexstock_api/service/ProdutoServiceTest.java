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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
                .sku("ARROZ-5KG")
                .codigoBarras("7891000000001")
                .descricao("Pacote tradicional")
                .unidadeMedida("UN")
                .precoCusto(BigDecimal.valueOf(10))
                .precoVenda(BigDecimal.valueOf(15))
                .estoqueAtual(BigDecimal.TEN)
                .estoqueMinimo(BigDecimal.ONE)
                .ativo(true)
                .permiteVendaSemEstoque(false)
                .versao(1L)
                .build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve criar produto completo quando sku e codigo de barras nao existem")
        void deveCriarProdutoComSucesso() {
            var request = requestBase();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.existsBySkuAndEmpresaIdAndDeletadoEmIsNull(
                    request.getSku(), empresaId)).thenReturn(false);
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(
                    request.getCodigoBarras(), empresaId)).thenReturn(false);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
                Produto salvo = invocation.getArgument(0);
                salvo.setId(UUID.randomUUID());
                return salvo;
            });

            var response = produtoService.criar(request);

            assertThat(response.getNome()).isEqualTo(request.getNome());
            assertThat(response.getSku()).isEqualTo(request.getSku());
            assertThat(response.getPrecoCusto()).isEqualTo(request.getPrecoCusto());
            assertThat(response.getEstoqueAtual()).isEqualTo(request.getEstoqueAtual());
            assertThat(response.getStatusEstoque()).isEqualTo("NORMAL");
            verify(produtoRepository).save(any(Produto.class));
        }

        @Test
        @DisplayName("deve bloquear sku duplicado na empresa")
        void deveBloquearSkuDuplicado() {
            var request = requestBase();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.existsBySkuAndEmpresaIdAndDeletadoEmIsNull(
                    request.getSku(), empresaId)).thenReturn(true);

            assertThatThrownBy(() -> produtoService.criar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("SKU");
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve bloquear codigo de barras duplicado na empresa")
        void deveBloquearCodigoBarrasDuplicado() {
            var request = requestBase();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.existsBySkuAndEmpresaIdAndDeletadoEmIsNull(
                    request.getSku(), empresaId)).thenReturn(false);
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(
                    request.getCodigoBarras(), empresaId)).thenReturn(true);

            assertThatThrownBy(() -> produtoService.criar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Codigo de barras");
            verify(produtoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("uploadImagem")
    class UploadImagem {

        @Test
        @DisplayName("deve enviar imagem e persistir url e chave no produto")
        void deveEnviarImagemEPersistirUrlEChave() {
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

            var response = produtoService.uploadImagem(empresaId, produtoId, arquivo);

            assertThat(response.produtoId()).isEqualTo(produtoId);
            assertThat(response.imagemUrl()).isEqualTo(upload.url());
            assertThat(response.imagemKey()).isEqualTo(upload.key());
            assertThat(produto.getVersao()).isEqualTo(2L);
            verify(produtoRepository).save(produto);
        }

        @Test
        @DisplayName("deve falhar quando produto nao existe")
        void deveFalharQuandoProdutoNaoExiste() {
            var arquivo = new MockMultipartFile(
                    "arquivo", "produto.png", "image/png", "conteudo".getBytes());
            when(produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.uploadImagem(empresaId, produtoId, arquivo))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
            verifyNoInteractions(storageService);
            verify(produtoRepository, never()).save(any());
        }
    }

    private ProdutoRequest requestBase() {
        return ProdutoRequest.builder()
                .empresaId(empresaId)
                .nome("Feijao 1kg")
                .sku("FEIJAO-1KG")
                .codigoBarras("7891000000002")
                .descricao("Pacote premium")
                .unidadeMedida("UN")
                .precoCusto(BigDecimal.valueOf(8.50))
                .precoVenda(BigDecimal.valueOf(11.90))
                .precoVendaAtacado(BigDecimal.valueOf(10.99))
                .estoqueAtual(BigDecimal.valueOf(20))
                .estoqueMinimo(BigDecimal.valueOf(5))
                .estoqueMaximo(BigDecimal.valueOf(50))
                .ativo(true)
                .permiteVendaSemEstoque(false)
                .build();
    }
}
