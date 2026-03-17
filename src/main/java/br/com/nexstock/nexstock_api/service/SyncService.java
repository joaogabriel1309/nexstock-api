package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.StatusSync;
import br.com.nexstock.nexstock_api.dto.request.*;
import br.com.nexstock.nexstock_api.dto.response.*;
import br.com.nexstock.nexstock_api.exception.SyncException;
import br.com.nexstock.nexstock_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final ContratoService                contratoService;
    private final DispositivoService             dispositivoService;
    private final ProdutoService                 produtoService;
    private final ProdutoRepository              produtoRepository;
    private final MovimentacaoEstoqueRepository  movimentacaoRepository;
    private final DispositivoRepository          dispositivoRepository;
    private final SyncLogRepository              syncLogRepository;

    @Transactional
    public SyncResponse processar(SyncRequest request) {
        LocalDateTime inicioSync = LocalDateTime.now();

        Contrato contrato;
        Dispositivo dispositivo;
        try {
            contrato    = contratoService.buscarEntidadeVigente(request.getContratoId());
            dispositivo = dispositivoService.buscarEntidade(request.getContratoId(), request.getDispositivoId());
        } catch (Exception ex) {
            throw new SyncException("Falha na autenticação do sync: " + ex.getMessage(), ex);
        }

        log.info("Sync iniciado — contrato: {} | dispositivo: {} | produtos: {} | movimentações: {}",
                contrato.getId(), dispositivo.getId(),
                request.getProdutos().size(), request.getMovimentacoes().size());

        List<ConflictInfo> conflitos = new ArrayList<>();

        int produtosProcessados = processarProdutos(
            request.getProdutos(), contrato, dispositivo, conflitos
        );

        int movimentacoesRegistradas = processarMovimentacoes(
            request.getMovimentacoes(), contrato, dispositivo
        );

        LocalDateTime baseSync = request.getUltimoSyncCliente() != null
            ? request.getUltimoSyncCliente()
            : LocalDateTime.of(2000, 1, 1, 0, 0);

        List<ProdutoResponse> produtosServidor = produtoRepository
                .findAllParaSync(contrato.getId(), baseSync)
                .stream()
                .map(ProdutoResponse::from)
                .toList();

        dispositivo.registrarSync();
        dispositivoRepository.save(dispositivo);

        SyncLog syncLog = SyncLog.builder()
                .contrato(contrato)
                .dispositivo(dispositivo)
                .dataSync(inicioSync)
                .registrosEnviados(request.getProdutos().size() + request.getMovimentacoes().size())
                .registrosRecebidos(produtosServidor.size())
                .status(StatusSync.SUCESSO)
                .build();

        SyncLog logSalvo = syncLogRepository.save(syncLog);

        log.info("Sync concluído — processados: {} produtos, {} movimentações | delta: {} | conflitos: {}",
                produtosProcessados, movimentacoesRegistradas,
                produtosServidor.size(), conflitos.size());

        return SyncResponse.builder()
                .syncLogId(logSalvo.getId())
                .serverTimestamp(inicioSync)
                .produtosServidor(produtosServidor)
                .produtosProcessados(produtosProcessados)
                .movimentacoesRegistradas(movimentacoesRegistradas)
                .conflitos(conflitos)
                .status("SUCESSO")
                .build();
    }

    private int processarProdutos(
            List<ProdutoSyncRequest> lista,
            Contrato contrato,
            Dispositivo dispositivo,
            List<ConflictInfo> conflitos) {

        int count = 0;
        for (ProdutoSyncRequest dto : lista) {
            try {
                processarUmProduto(dto, contrato, dispositivo, conflitos);
                count++;
            } catch (Exception ex) {
                log.error("Falha ao processar produto {} no sync: {}", dto.getId(), ex.getMessage());
            }
        }
        return count;
    }

    private void processarUmProduto(
            ProdutoSyncRequest dto,
            Contrato contrato,
            Dispositivo dispositivo,
            List<ConflictInfo> conflitos) {

        Produto existente = produtoService.buscarEntidadeQualquer(contrato.getId(), dto.getId());

        if (existente == null) {
            Produto novo = Produto.builder()
                    .id(dto.getId())
                    .contrato(contrato)
                    .nome(dto.getNome())
                    .codigoBarras(dto.getCodigoBarras())
                    .estoque(dto.getEstoque())
                    .atualizadoEm(dto.getAtualizadoEm())
                    .versao(dto.getVersao() != null ? dto.getVersao() + 1 : 1L)
                    .dispositivoUltimaAlteracao(dispositivo)
                    .deletado(Boolean.TRUE.equals(dto.getDeletado()))
                    .build();
            produtoRepository.save(novo);
            return;
        }

        LocalDateTime clienteTs  = dto.getAtualizadoEm();
        LocalDateTime servidorTs = existente.getAtualizadoEm();

        if (clienteTs.isAfter(servidorTs)) {
            long versaoAnterior = existente.getVersao();
            existente.aplicarDadosSync(
                dto.getNome(), dto.getCodigoBarras(), dto.getEstoque(),
                dto.getAtualizadoEm(), dto.getDeletado(), dispositivo
            );
            produtoRepository.save(existente);

            if (versaoAnterior != dto.getVersao()) {
                conflitos.add(ConflictInfo.builder()
                    .produtoId(dto.getId())
                    .resolucao("CLIENTE_VENCEU")
                    .clienteAtualizadoEm(clienteTs)
                    .servidorAtualizadoEm(servidorTs)
                    .versaoFinal(existente.getVersao())
                    .build());
            }
        } else if (clienteTs.isBefore(servidorTs)) {
            conflitos.add(ConflictInfo.builder()
                .produtoId(dto.getId())
                .resolucao("SERVIDOR_VENCEU")
                .clienteAtualizadoEm(clienteTs)
                .servidorAtualizadoEm(servidorTs)
                .versaoFinal(existente.getVersao())
                .build());
        }
    }

    private int processarMovimentacoes(
            List<MovimentacaoSyncRequest> lista,
            Contrato contrato,
            Dispositivo dispositivo) {

        if (lista.isEmpty()) return 0;

        List<UUID> ids = lista.stream().map(MovimentacaoSyncRequest::getId).toList();
        Set<UUID>  jaExistem = movimentacaoRepository.findIdsExistentes(ids, contrato.getId());

        List<MovimentacaoEstoque> novas = new ArrayList<>();

        for (MovimentacaoSyncRequest dto : lista) {
            if (jaExistem.contains(dto.getId())) {
                log.debug("Movimentação {} já existe — ignorando duplicata", dto.getId());
                continue;
            }

            Produto produto = produtoService.buscarEntidadeQualquer(contrato.getId(), dto.getProdutoId());
            if (produto == null) {
                log.warn("Produto {} não encontrado para movimentação {} — ignorando", dto.getProdutoId(), dto.getId());
                continue;
            }

            novas.add(MovimentacaoEstoque.builder()
                    .id(dto.getId())
                    .contrato(contrato)
                    .produto(produto)
                    .tipo(dto.getTipo())
                    .quantidade(dto.getQuantidade())
                    .criadoEm(dto.getCriadoEm())
                    .dispositivo(dispositivo)
                    .build());
        }

        if (!novas.isEmpty()) {
            movimentacaoRepository.saveAll(novas);
        }

        return novas.size();
    }
}
