package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceTest {

    @Mock ProdutoRepository  produtoRepository;
    @Mock ContratoService    contratoService;
    @Mock DispositivoService dispositivoService;

    @InjectMocks ProdutoService produtoService;

    private UUID contratoId;
    private UUID dispositivoId;
    private Contrato contrato;
    private Dispositivo dispositivo;

    @BeforeEach
    void setUp() {
        contratoId   = UUID.randomUUID();
        dispositivoId = UUID.randomUUID();
        contrato     = Contrato.builder().id(contratoId).build();
        dispositivo  = Dispositivo.builder().id(dispositivoId).contrato(contrato).build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve criar produto com versão 1 e deletado=false")
        void deveCriarProdutoComVersaoInicial() {
            var request = ProdutoRequest.builder()
                    .contratoId(contratoId)
                    .dispositivoId(dispositivoId)
                    .nome("Arroz 5kg")
                    .codigoBarras("7896000123456")
                    .estoque(new BigDecimal("100"))
                    .build();

            when(contratoService.buscarEntidadeVigente(contratoId)).thenReturn(contrato);
            when(dispositivoService.buscarEntidade(contratoId, dispositivoId)).thenReturn(dispositivo);
            when(produtoRepository.findAllByContratoIdAndDeletadoFalse(contratoId))
                    .thenReturn(List.of()); // sem duplicata de código de barras
            when(produtoRepository.save(any())).thenAnswer(inv -> {
                Produto p = inv.getArgument(0);
                p = Produto.builder()
                        .id(UUID.randomUUID())
                        .contrato(p.getContrato())
                        .nome(p.getNome())
                        .codigoBarras(p.getCodigoBarras())
                        .estoque(p.getEstoque())
                        .atualizadoEm(p.getAtualizadoEm())
                        .versao(p.getVersao())
                        .deletado(p.getDeletado())
                        .dispositivoUltimaAlteracao(p.getDispositivoUltimaAlteracao())
                        .build();
                return p;
            });

            ProdutoResponse response = produtoService.criar(request);

            assertThat(response.getVersao()).isEqualTo(1L);
            assertThat(response.getDeletado()).isFalse();
            assertThat(response.getNome()).isEqualTo("Arroz 5kg");
        }

        @Test
        @DisplayName("deve lançar exceção quando código de barras já existe no contrato")
        void deveLancarExcecaoComCodigoBarrasDuplicado() {
            var produtoExistente = Produto.builder()
                    .id(UUID.randomUUID())
                    .codigoBarras("7896000123456")
                    .deletado(false)
                    .build();

            var request = ProdutoRequest.builder()
                    .contratoId(contratoId)
                    .dispositivoId(dispositivoId)
                    .nome("Arroz 5kg")
                    .codigoBarras("7896000123456")
                    .estoque(new BigDecimal("100"))
                    .build();

            when(contratoService.buscarEntidadeVigente(contratoId)).thenReturn(contrato);
            when(dispositivoService.buscarEntidade(contratoId, dispositivoId)).thenReturn(dispositivo);
            when(produtoRepository.findAllByContratoIdAndDeletadoFalse(contratoId))
                    .thenReturn(List.of(produtoExistente));

            assertThatThrownBy(() -> produtoService.criar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já está em uso");
        }
    }

    @Nested
    @DisplayName("deletar (soft delete)")
    class Deletar {

        @Test
        @DisplayName("deve marcar produto como deletado sem remover do banco")
        void deveMarcarComoDeletadoSemRemover() {
            var produtoId = UUID.randomUUID();
            var produto   = Produto.builder()
                    .id(produtoId)
                    .contrato(contrato)
                    .nome("Feijão 1kg")
                    .atualizadoEm(LocalDateTime.now().minusHours(1))
                    .versao(1L)
                    .deletado(false)
                    .build();

            when(produtoRepository.findByIdAndContratoId(produtoId, contratoId))
                    .thenReturn(Optional.of(produto));
            when(dispositivoService.buscarEntidade(contratoId, dispositivoId))
                    .thenReturn(dispositivo);
            when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            produtoService.deletar(contratoId, produtoId, dispositivoId);

            assertThat(produto.getDeletado()).isTrue();
            assertThat(produto.getVersao()).isEqualTo(2L);
            verify(produtoRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deve lançar exceção ao tentar deletar produto já deletado")
        void deveLancarExcecaoAoDeletarProdutoJaDeletado() {
            var produtoId = UUID.randomUUID();
            var produto   = Produto.builder()
                    .id(produtoId)
                    .contrato(contrato)
                    .deletado(true)
                    .build();

            when(produtoRepository.findByIdAndContratoId(produtoId, contratoId))
                    .thenReturn(Optional.of(produto));

            assertThatThrownBy(() -> produtoService.deletar(contratoId, produtoId, dispositivoId))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("listarParaSync")
    class ListarParaSync {

        @Test
        @DisplayName("deve incluir produtos deletados no delta de sync")
        void deveIncluirDeletadosNoDeltaSync() {
            var desde = LocalDateTime.now().minusHours(1);
            var ativo  = Produto.builder().id(UUID.randomUUID()).contrato(contrato)
                    .nome("Ativo").atualizadoEm(LocalDateTime.now()).versao(1L).deletado(false).build();
            var deletado = Produto.builder().id(UUID.randomUUID()).contrato(contrato)
                    .nome("Deletado").atualizadoEm(LocalDateTime.now()).versao(2L).deletado(true).build();

            when(produtoRepository.findAllParaSync(contratoId, desde))
                    .thenReturn(List.of(ativo, deletado));

            List<ProdutoResponse> result = produtoService.listarParaSync(contratoId, desde);

            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(ProdutoResponse::getDeletado);
        }
    }
}