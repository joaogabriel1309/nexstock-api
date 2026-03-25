package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
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

    @InjectMocks ProdutoService produtoService;

    private UUID empresaId;
    private UUID dispositivoId;
    private UUID produtoId;
    private Empresa empresa;
    private Dispositivo dispositivo;
    private Produto produtoAtivo;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        dispositivoId = UUID.randomUUID();
        produtoId = UUID.randomUUID();

        empresa = Empresa.builder().id(empresaId).nome("Loja Matriz").build();
        dispositivo = Dispositivo.builder().id(dispositivoId).empresa(empresa).build();

        produtoAtivo = Produto.builder()
                .id(produtoId)
                .empresa(empresa)
                .nome("Coca-Cola 2L")
                .codigoBarras("789101010")
                .estoque(new BigDecimal("50"))
                .versao(1L)
                .build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve criar produto com versão 1 e deletadoEm nulo")
        void deveCriarProdutoComSucesso() {
            var request = ProdutoRequest.builder()
                    .empresaId(empresaId)
                    .dispositivoId(dispositivoId)
                    .nome("Coca-Cola 2L")
                    .codigoBarras("789101010")
                    .estoque(new BigDecimal("50"))
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(dispositivoService.buscarEntidade(empresaId, dispositivoId)).thenReturn(dispositivo);
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull("789101010", empresaId))
                    .thenReturn(false);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

            ProdutoResponse response = produtoService.criar(request);

            assertThat(response.getNome()).isEqualTo("Coca-Cola 2L");
            assertThat(response.getVersao()).isEqualTo(1L);
            verify(produtoRepository).save(any(Produto.class));
        }

        @Test
        @DisplayName("deve lançar exceção se código de barras já existir na mesma empresa")
        void deveLancarExcecaoCodigoBarrasDuplicado() {
            var request = ProdutoRequest.builder()
                    .empresaId(empresaId)
                    .dispositivoId(dispositivoId)
                    .codigoBarras("789101010")
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(dispositivoService.buscarEntidade(empresaId, dispositivoId)).thenReturn(dispositivo);
            when(produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull("789101010", empresaId))
                    .thenReturn(true);

            assertThatThrownBy(() -> produtoService.criar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já cadastrado nesta empresa");
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar dados e incrementar a versão para o Sync")
        void deveAtualizarEIncrementarVersao() {
            var request = ProdutoRequest.builder()
                    .dispositivoId(dispositivoId)
                    .nome("Coca-Cola Zero 2L")
                    .codigoBarras("789101010")
                    .estoque(new BigDecimal("45"))
                    .build();

            when(produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId))
                    .thenReturn(Optional.of(produtoAtivo));
            when(dispositivoService.buscarEntidade(empresaId, dispositivoId)).thenReturn(dispositivo);

            when(produtoRepository.existsCodigoBarrasEmOutroProduto(empresaId, "789101010", produtoId))
                    .thenReturn(false);

            when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

            ProdutoResponse response = produtoService.atualizar(empresaId, produtoId, request);

            assertThat(response.getNome()).isEqualTo("Coca-Cola Zero 2L");
            assertThat(response.getVersao()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("deletar (Soft Delete)")
    class Deletar {

        @Test
        @DisplayName("deve preencher deletadoEm, incrementar versão e não excluir do banco")
        void deveFazerSoftDelete() {
            when(produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId))
                    .thenReturn(Optional.of(produtoAtivo));
            when(dispositivoService.buscarEntidade(empresaId, dispositivoId)).thenReturn(dispositivo);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

            produtoService.deletar(empresaId, produtoId, dispositivoId);

            assertThat(produtoAtivo.getDeletadoEm()).isNotNull();
            assertThat(produtoAtivo.getVersao()).isEqualTo(2L);
            verify(produtoRepository, never()).delete(any());
            verify(produtoRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("consultas")
    class Consultas {

        @Test
        @DisplayName("listarParaSync deve retornar todos os itens modificados desde a data")
        void listarParaSync() {
            LocalDateTime desde = LocalDateTime.now().minusHours(1);
            when(produtoRepository.findAllParaSync(empresaId, desde)).thenReturn(List.of(produtoAtivo));

            List<ProdutoResponse> result = produtoService.listarParaSync(empresaId, desde);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(produtoId);
        }
    }
}