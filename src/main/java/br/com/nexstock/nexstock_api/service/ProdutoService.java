package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ContratoService   contratoService;
    private final DispositivoService dispositivoService;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Contrato   contrato   = contratoService.buscarEntidadeVigente(request.getContratoId());
        Dispositivo dispositivo = dispositivoService.buscarEntidade(request.getContratoId(), request.getDispositivoId());

        validarCodigoBarras(request.getCodigoBarras(), contrato.getId(), null);

        Produto produto = Produto.builder()
                .contrato(contrato)
                .nome(request.getNome())
                .codigoBarras(request.getCodigoBarras())
                .estoque(request.getEstoque())
                .atualizadoEm(LocalDateTime.now())
                .versao(1L)
                .dispositivoUltimaAlteracao(dispositivo)
                .build();

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(UUID contratoId, UUID id, ProdutoRequest request) {
        Produto     produto     = buscarEntidadeAtiva(contratoId, id);
        Dispositivo dispositivo = dispositivoService.buscarEntidade(contratoId, request.getDispositivoId());

        validarCodigoBarras(request.getCodigoBarras(), contratoId, id);

        produto.setNome(request.getNome());
        produto.setCodigoBarras(request.getCodigoBarras());
        produto.setEstoque(request.getEstoque());

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(UUID contratoId, UUID id) {
        return ProdutoResponse.from(buscarEntidadeAtiva(contratoId, id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarAtivos(UUID contratoId) {
        return produtoRepository.findAllByContratoIdAndDeletadoFalse(contratoId)
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarParaSync(UUID contratoId, LocalDateTime desde) {
        return produtoRepository.findAllParaSync(contratoId, desde)
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional
    public void deletar(UUID contratoId, UUID id, UUID dispositivoId) {
        Produto     produto     = buscarEntidadeAtiva(contratoId, id);
        Dispositivo dispositivo = dispositivoService.buscarEntidade(contratoId, dispositivoId);

        produtoRepository.save(produto);
        log.info("Produto {} marcado como deletado (versão {})", id, produto.getVersao());
    }

    @Transactional(readOnly = true)
    public Produto buscarEntidadeAtiva(UUID contratoId, UUID id) {
        Produto produto = produtoRepository.findByIdAndContratoId(id, contratoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));

        return produto;
    }

    @Transactional(readOnly = true)
    public Produto buscarEntidadeQualquer(UUID contratoId, UUID id) {
        return produtoRepository.findByIdAndContratoId(id, contratoId).orElse(null);
    }

    private void validarCodigoBarras(String codigoBarras, UUID contratoId, UUID idExcluir) {
        if (codigoBarras == null || codigoBarras.isBlank()) return;

        if (idExcluir == null) {
            produtoRepository
                .findAllByContratoIdAndDeletadoFalse(contratoId)
                .stream()
                .filter(p -> codigoBarras.equals(p.getCodigoBarras()))
                .findFirst()
                .ifPresent(p -> { throw new RegraDeNegocioException(
                    "Código de barras '" + codigoBarras + "' já está em uso."
                ); });
        } else if (produtoRepository.existsCodigoBarrasEmOutroProduto(contratoId, codigoBarras, idExcluir)) {
            throw new RegraDeNegocioException(
                "Código de barras '" + codigoBarras + "' já está em uso."
            );
        }
    }
}
