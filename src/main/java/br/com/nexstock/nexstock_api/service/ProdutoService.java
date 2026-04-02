package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
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
    private final EmpresaRepository empresaRepository;
    private final DispositivoService dispositivoService;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        var empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", request.getEmpresaId()));

        validarCodigoBarras(request.getCodigoBarras(), empresa.getId(), null);

        Produto produto = Produto.builder()
                .empresa(empresa)
                .nome(request.getNome())
                .codigoBarras(request.getCodigoBarras())
                .estoque(request.getEstoque())
                .atualizadoEm(LocalDateTime.now())
                .versao(1L)
                .build();

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(UUID empresaId, UUID id, ProdutoRequest request) {
        Produto produto = buscarEntidadeAtiva(empresaId, id);

        validarCodigoBarras(request.getCodigoBarras(), empresaId, id);

        produto.setNome(request.getNome());
        produto.setCodigoBarras(request.getCodigoBarras());
        produto.setEstoque(request.getEstoque());
        produto.setVersao(produto.getVersao() + 1);
        produto.setAtualizadoEm(LocalDateTime.now());

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public void deletar(UUID empresaId, UUID id) {
        Produto produto = buscarEntidadeAtiva(empresaId, id);

        produto.setDeletadoEm(LocalDateTime.now());
        produto.setVersao(produto.getVersao() + 1);

        produtoRepository.save(produto);

        log.info("Produto {} marcado como deletado na empresa {}", id, empresaId);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(UUID empresaId, UUID produtoId) {
        return produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(produtoId, empresaId)
                .map(ProdutoResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarAtivos(UUID empresaId) {
        return produtoRepository.findAllByEmpresaIdAndDeletadoEmIsNull(empresaId)
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarParaSync(UUID empresaId, LocalDateTime desde) {
        return produtoRepository.findAllParaSync(empresaId, desde)
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Produto buscarEntidadeAtiva(UUID empresaId, UUID id) {
        return produtoRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(id, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private void validarCodigoBarras(String codigoBarras, UUID empresaId, UUID idExcluir) {
        if (codigoBarras == null || codigoBarras.isBlank()) return;

        boolean existe = (idExcluir == null)
                ? produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(codigoBarras, empresaId)
                : produtoRepository.existsCodigoBarrasEmOutroProduto(empresaId, codigoBarras, idExcluir);

        if (existe) {
            throw new RegraDeNegocioException("Código de barras '" + codigoBarras + "' já cadastrado nesta empresa.");
        }
    }
}