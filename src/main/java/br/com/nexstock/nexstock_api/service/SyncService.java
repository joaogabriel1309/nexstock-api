package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.MovimentacaoEstoque;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.domain.entity.SyncLog;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.StatusSync;
import br.com.nexstock.nexstock_api.dto.request.MovimentacaoSyncRequest;
import br.com.nexstock.nexstock_api.dto.request.ProdutoSyncRequest;
import br.com.nexstock.nexstock_api.dto.request.SyncRequest;
import br.com.nexstock.nexstock_api.dto.response.ConflictInfoResponse;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.dto.response.SyncResponse;
import br.com.nexstock.nexstock_api.exception.SyncException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.MovimentacaoEstoqueRepository;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import br.com.nexstock.nexstock_api.repository.SyncLogRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final EmpresaRepository empresaRepository;
    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final SyncLogRepository syncLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntityManager entityManager;

    @Transactional
    public SyncResponse processar(SyncRequest request) {
        LocalDateTime inicioSync = LocalDateTime.now();

        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new SyncException("Empresa nao encontrada ou inativa."));
        Usuario usuario = buscarUsuarioAutenticado();

        log.info("Iniciando Sync - Empresa: {} | Usuario: {} | Itens recebidos: {}",
                empresa.getNome(), usuario.getEmail(), request.getProdutos().size());

        List<ConflictInfoResponse> conflitos = new ArrayList<>();

        int produtosProcessados = processarProdutos(
                request.getProdutos(), empresa, usuario, conflitos
        );

        int movimentacoesRegistradas = processarMovimentacoes(
                request.getMovimentacoes(), empresa, usuario
        );

        LocalDateTime baseSync = request.getUltimoSyncCliente() != null
                ? request.getUltimoSyncCliente()
                : LocalDateTime.of(2000, 1, 1, 0, 0);

        List<ProdutoResponse> produtosServidor = produtoRepository
                .findAllParaSync(empresa.getId(), baseSync)
                .stream()
                .map(ProdutoResponse::from)
                .toList();

        SyncLog logSalvo = syncLogRepository.save(SyncLog.builder()
                .empresa(empresa)
                .usuario(usuario)
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
            Usuario usuario,
            List<ConflictInfoResponse> conflitos) {

        int count = 0;
        for (ProdutoSyncRequest dto : lista) {
            try {
                processarUmProduto(dto, empresa, usuario, conflitos);
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
            Usuario usuario,
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
                    .usuarioUltimaAlteracao(usuario)
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
            existente.setUsuarioUltimaAlteracao(usuario);
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
            Usuario usuario) {

        if (lista.isEmpty()) return 0;

        List<UUID> idsEnviados = lista.stream().map(MovimentacaoSyncRequest::getId).toList();
        Set<UUID> jaExistentes = movimentacaoRepository.findIdsExistentes(idsEnviados, empresa.getId());

        int count = 0;

        for (MovimentacaoSyncRequest dto : lista) {
            if (jaExistentes.contains(dto.getId())) continue;

            Produto produto = produtoService.buscarEntidadeAtiva(empresa.getId(), dto.getProdutoId());
            if (produto == null) continue;

            produto.setUsuarioUltimaAlteracao(usuario);

            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                    .id(dto.getId())
                    .produto(produto)
                    .tipo(dto.getTipo())
                    .quantidade(dto.getQuantidade())
                    .empresa(empresa)
                    .build();

            entityManager.persist(movimentacao);
            count++;
        }

        return count;
    }

    private Usuario buscarUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmailAndDeletadoEmIsNull(email)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Usuario nao autenticado"));
    }
}
