package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.TipoMovimentacao;
import br.com.nexstock.nexstock_api.dto.request.*;
import br.com.nexstock.nexstock_api.dto.response.SyncResponse;
import br.com.nexstock.nexstock_api.repository.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncService")
class SyncServiceTest {

    @Mock ContratoService               contratoService;
    @Mock DispositivoService            dispositivoService;
    @Mock ProdutoService                produtoService;
    @Mock ProdutoRepository             produtoRepository;
    @Mock MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock DispositivoRepository         dispositivoRepository;
    @Mock SyncLogRepository             syncLogRepository;

    @InjectMocks SyncService syncService;

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

        when(contratoService.buscarEntidadeVigente(contratoId)).thenReturn(contrato);
        when(dispositivoService.buscarEntidade(contratoId, dispositivoId)).thenReturn(dispositivo);
        when(produtoRepository.findAllParaSync(any(), any())).thenReturn(List.of());
        when(dispositivoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(syncLogRepository.save(any())).thenAnswer(inv -> {
            SyncLog log = inv.getArgument(0);
            return SyncLog.builder()
                    .id(UUID.randomUUID())
                    .contrato(log.getContrato())
                    .dispositivo(log.getDispositivo())
                    .dataSync(log.getDataSync())
                    .registrosEnviados(log.getRegistrosEnviados())
                    .registrosRecebidos(log.getRegistrosRecebidos())
                    .status(log.getStatus())
                    .build();
        });
    }

    private SyncRequest requestBase() {
        return SyncRequest.builder()
                .contratoId(contratoId)
                .dispositivoId(dispositivoId)
                .ultimoSyncCliente(LocalDateTime.now().minusHours(1))
                .produtos(new ArrayList<>())
                .movimentacoes(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("processar — produtos (LWW)")
    class ProdutosLWW {

        @Test
        @DisplayName("deve criar produto novo quando não existe no servidor")
        void deveCriarProdutoNovo() {
            var produtoNovo = ProdutoSyncRequest.builder()
                    .id(UUID.randomUUID())
                    .nome("Arroz 5kg")
                    .estoque(new BigDecimal("100"))
                    .atualizadoEm(LocalDateTime.now())
                    .versao(1L)
                    .deletado(false)
                    .build();

            when(produtoService.buscarEntidadeQualquer(contratoId, produtoNovo.getId()))
                    .thenReturn(null); // não existe no servidor
            when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var request = requestBase();
            request.getProdutos().add(produtoNovo);

            SyncResponse response = syncService.processar(request);

            assertThat(response.getProdutosProcessados()).isEqualTo(1);
            assertThat(response.getConflitos()).isEmpty();
            verify(produtoRepository).save(argThat(p -> p.getId().equals(produtoNovo.getId())));
        }

        @Test
        @DisplayName("deve aceitar dados do cliente quando cliente é mais recente (LWW)")
        void deveAceitarClienteQuandoMaisRecente() {
            var produtoId       = UUID.randomUUID();
            var tsServidor      = LocalDateTime.now().minusHours(2);
            var tsCliente       = LocalDateTime.now().minusHours(1); // mais recente

            var produtoServidor = Produto.builder()
                    .id(produtoId).contrato(contrato)
                    .nome("Nome antigo").atualizadoEm(tsServidor).versao(1L).deletado(false)
                    .build();

            var produtoCliente = ProdutoSyncRequest.builder()
                    .id(produtoId).nome("Nome novo")
                    .estoque(BigDecimal.TEN).atualizadoEm(tsCliente)
                    .versao(2L).deletado(false)
                    .build();

            when(produtoService.buscarEntidadeQualquer(contratoId, produtoId))
                    .thenReturn(produtoServidor);
            when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var request = requestBase();
            request.getProdutos().add(produtoCliente);

            SyncResponse response = syncService.processar(request);

            assertThat(produtoServidor.getNome()).isEqualTo("Nome novo");
            assertThat(produtoServidor.getVersao()).isEqualTo(2L); // incrementou
        }

        @Test
        @DisplayName("deve manter servidor quando servidor é mais recente (LWW)")
        void deveManterServidorQuandoMaisRecente() {
            var produtoId   = UUID.randomUUID();
            var tsServidor  = LocalDateTime.now().minusHours(1); // mais recente
            var tsCliente   = LocalDateTime.now().minusHours(2);

            var produtoServidor = Produto.builder()
                    .id(produtoId).contrato(contrato)
                    .nome("Nome servidor").atualizadoEm(tsServidor).versao(3L).deletado(false)
                    .build();

            var produtoCliente = ProdutoSyncRequest.builder()
                    .id(produtoId).nome("Nome cliente")
                    .estoque(BigDecimal.TEN).atualizadoEm(tsCliente)
                    .versao(1L).deletado(false)
                    .build();

            when(produtoService.buscarEntidadeQualquer(contratoId, produtoId))
                    .thenReturn(produtoServidor);

            var request = requestBase();
            request.getProdutos().add(produtoCliente);

            SyncResponse response = syncService.processar(request);

            // Servidor mantém o nome — não foi alterado
            assertThat(produtoServidor.getNome()).isEqualTo("Nome servidor");
            // Conflito registrado
            assertThat(response.getConflitos()).hasSize(1);
            assertThat(response.getConflitos().get(0).getResolucao()).isEqualTo("SERVIDOR_VENCEU");
        }
    }

    @Nested
    @DisplayName("processar — movimentações (idempotência)")
    class MovimentacoesIdempotencia {

        @Test
        @DisplayName("deve ignorar movimentação duplicada (mesmo UUID)")
        void deveIgnorarMovimentacaoDuplicada() {
            var movId    = UUID.randomUUID();
            var produtoId = UUID.randomUUID();

            var mov = MovimentacaoSyncRequest.builder()
                    .id(movId)
                    .produtoId(produtoId)
                    .tipo(TipoMovimentacao.ENTRADA)
                    .quantidade(new BigDecimal("10"))
                    .criadoEm(LocalDateTime.now())
                    .build();

            // Simula que já existe no banco
            when(movimentacaoRepository.findIdsExistentes(List.of(movId), contratoId))
                    .thenReturn(Set.of(movId));

            var request = requestBase();
            request.getMovimentacoes().add(mov);

            SyncResponse response = syncService.processar(request);

            assertThat(response.getMovimentacoesRegistradas()).isEqualTo(0);
            verify(movimentacaoRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("deve registrar movimentação nova")
        void deveRegistrarMovimentacaoNova() {
            var movId     = UUID.randomUUID();
            var produtoId = UUID.randomUUID();
            var produto   = Produto.builder().id(produtoId).contrato(contrato).build();

            var mov = MovimentacaoSyncRequest.builder()
                    .id(movId)
                    .produtoId(produtoId)
                    .tipo(TipoMovimentacao.SAIDA)
                    .quantidade(new BigDecimal("5"))
                    .criadoEm(LocalDateTime.now())
                    .build();

            when(movimentacaoRepository.findIdsExistentes(List.of(movId), contratoId))
                    .thenReturn(Set.of()); // não existe ainda
            when(produtoService.buscarEntidadeQualquer(contratoId, produtoId))
                    .thenReturn(produto);
            when(movimentacaoRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

            var request = requestBase();
            request.getMovimentacoes().add(mov);

            SyncResponse response = syncService.processar(request);

            assertThat(response.getMovimentacoesRegistradas()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("processar — geral")
    class Geral {

        @Test
        @DisplayName("deve retornar status SUCESSO e gravar SyncLog")
        void deveRetornarSucessoEGravarLog() {
            SyncResponse response = syncService.processar(requestBase());

            assertThat(response.getStatus()).isEqualTo("SUCESSO");
            assertThat(response.getSyncLogId()).isNotNull();
            assertThat(response.getServerTimestamp()).isNotNull();
            verify(syncLogRepository).save(any());
        }

        @Test
        @DisplayName("deve atualizar ultimoSync do dispositivo após sync bem-sucedido")
        void deveAtualizarUltimoSyncDoDispositivo() {
            syncService.processar(requestBase());

            verify(dispositivoRepository).save(argThat(d -> d.getUltimoSync() != null));
        }
    }
}