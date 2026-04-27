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

    private final EmpresaRepository              empresaRepository;
    private final DispositivoService             dispositivoService;
    private final ProdutoService                 produtoService;
    private final ProdutoRepository              produtoRepository;
    private final MovimentacaoEstoqueRepository  movimentacaoRepository;
    private final DispositivoRepository          dispositivoRepository;
    private final SyncLogRepository              syncLogRepository;

    @Transactional
    public SyncResponse processar(SyncRequest request) {
        LocalDateTime inicioSync = LocalDateTime.now();

        var empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new SyncException("Empresa não encontrada ou inativa."));

        var dispositivo = dispositivoService.buscarEntidade(empresa.getId(), request.getDispositivoId());

        log.info("Iniciando Sync - Empresa: {} | Dispositivo: {} | Itens recebidos: {}",
                empresa.getNome(), dispositivo.getNome(), request.getProdutos().size());

        List<ConflictInfoResponse> conflitos = new ArrayList<>();

        int produtosProcessados = processarProdutos(
                request.getProdutos(), empresa, dispositivo, conflitos
        );

        int movimentacoesRegistradas = processarMovimentacoes(
                request.getMovimentacoes(), empresa, dispositivo
        );

        LocalDateTime baseSync = request.getUltimoSyncCliente() != null
                ? request.getUltimoSyncCliente()
                : LocalDateTime.of(2000, 1, 1, 0, 0);

        List<ProdutoResponse> produtosServidor = produtoRepository
                .findAllParaSync(empresa.getId(), baseSync)
                .stream()
                .map(ProdutoResponse::from)
                .toList();

        dispositivo.setUltimoSync(inicioSync);
        dispositivoRepository.save(dispositivo);

        SyncLog logSalvo = syncLogRepository.save(SyncLog.builder()
                .dispositivo(dispositivo)
                .dataSync(inicioSync)
                .registrosEnviados(request.getProdutos().size() + request.getMovimentacoes().size())
                .registrosRecebidos(produtosServidor.size())
                .status(StatusSync.SUCESSO)
                .build());

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
            Empresa empresa,
            Dispositivo dispositivo,
            List<ConflictInfoResponse> conflitos) {

        int count = 0;
        for (ProdutoSyncRequest dto : lista) {
            try {
                processarUmProduto(dto, empresa, dispositivo, conflitos);
                count++;
            } catch (Exception ex) {
                log.error("Erro ao sincronizar produto {}: {}", dto.getId(), ex.getMessage());
            }
        }
        return count;
    }

    private void processarUmProduto(
            ProdutoSyncRequest dto,
            Empresa empresa,
            Dispositivo dispositivo,
            List<ConflictInfoResponse> conflitos) {

        Produto existente = produtoService.buscarEntidadeInclusoDeletados(empresa.getId(), dto.getId());

        if (existente == null) {
            Produto novo = Produto.builder()
                    .id(dto.getId())
                    .empresa(empresa)
                    .nome(dto.getNome())
                    .sku(dto.getSku())
                    .codigoBarras(dto.getCodigoBarras())
                    .descricao(dto.getDescricao())
                    .unidadeMedida(dto.getUnidadeMedida())
                    .precoCusto(dto.getPrecoCusto())
                    .precoVenda(dto.getPrecoVenda())
                    .precoVendaAtacado(dto.getPrecoVendaAtacado())
                    .estoqueAtual(dto.getEstoqueAtual())
                    .estoqueMinimo(dto.getEstoqueMinimo())
                    .estoqueMaximo(dto.getEstoqueMaximo())
                    .ativo(dto.getAtivo())
                    .permiteVendaSemEstoque(dto.getPermiteVendaSemEstoque())
                    .atualizadoEm(dto.getAtualizadoEm())
                    .versao(1L)
                    .dispositivoUltimaAlteracao(dispositivo)
                    .deletadoEm(dto.getDeletadoEm())
                    .build();

            produtoRepository.save(novo);

            return;
        }

        LocalDateTime clienteTs = dto.getAtualizadoEm();
        LocalDateTime servidorTs = existente.getAtualizadoEm();

        if (clienteTs.isAfter(servidorTs)) {
            existente.setNome(dto.getNome());
            existente.setSku(dto.getSku());
            existente.setCodigoBarras(dto.getCodigoBarras());
            existente.setDescricao(dto.getDescricao());
            existente.setUnidadeMedida(dto.getUnidadeMedida());
            existente.setPrecoCusto(dto.getPrecoCusto());
            existente.setPrecoVenda(dto.getPrecoVenda());
            existente.setPrecoVendaAtacado(dto.getPrecoVendaAtacado());
            existente.setEstoqueAtual(dto.getEstoqueAtual());
            existente.setEstoqueMinimo(dto.getEstoqueMinimo());
            existente.setEstoqueMaximo(dto.getEstoqueMaximo());
            existente.setAtivo(dto.getAtivo());
            existente.setPermiteVendaSemEstoque(dto.getPermiteVendaSemEstoque());
            existente.setVersao(existente.getVersao() + 1);
            existente.setAtualizadoEm(clienteTs);
            existente.setDispositivoUltimaAlteracao(dispositivo);

            existente.setDeletadoEm(dto.getDeletadoEm());

            produtoRepository.save(existente);

        } else if (clienteTs.isBefore(servidorTs)) {
            conflitos.add(ConflictInfoResponse.builder()
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
            Empresa empresa,
            Dispositivo dispositivo) {

        if (lista.isEmpty()) return 0;

        List<UUID> idsEnviados = lista.stream().map(MovimentacaoSyncRequest::getId).toList();
        Set<UUID> jaExistentes = movimentacaoRepository.findIdsExistentes(idsEnviados, empresa.getId());

        List<MovimentacaoEstoque> novas = new ArrayList<>();

        for (MovimentacaoSyncRequest dto : lista) {
            if (jaExistentes.contains(dto.getId())) continue;

            Produto produto = produtoService.buscarEntidadeAtiva(empresa.getId(), dto.getProdutoId());
            if (produto == null) continue;

            novas.add(MovimentacaoEstoque.builder()
                    .id(dto.getId())
                    .produto(produto)
                    .tipo(dto.getTipo())
                    .quantidade(dto.getQuantidade())
                    .dispositivo(dispositivo)
                    .empresa(empresa)
                    .build());
        }

        if (!novas.isEmpty()) {
            movimentacaoRepository.saveAll(novas);
        }

        return novas.size();
    }
}
