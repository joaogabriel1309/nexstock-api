package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Produto;
import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoImagemResponse;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.ProdutoRepository;
import br.com.nexstock.nexstock_api.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final StorageService storageService;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        var empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", request.getEmpresaId()));

        validarSku(request.getSku(), empresa.getId(), null);
        validarCodigoBarras(request.getCodigoBarras(), empresa.getId(), null);
        validarEstoqueMaximo(request.getEstoqueMinimo(), request.getEstoqueMaximo());

        Produto produto = Produto.builder()
                .empresa(empresa)
                .nome(request.getNome())
                .sku(normalizarTexto(request.getSku()))
                .codigoBarras(normalizarOpcional(request.getCodigoBarras()))
                .descricao(normalizarOpcional(request.getDescricao()))
                .unidadeMedida(normalizarTexto(request.getUnidadeMedida()))
                .precoCusto(request.getPrecoCusto())
                .precoVenda(request.getPrecoVenda())
                .precoVendaAtacado(request.getPrecoVendaAtacado())
                .estoqueAtual(request.getEstoqueAtual())
                .estoqueMinimo(request.getEstoqueMinimo())
                .estoqueMaximo(request.getEstoqueMaximo())
                .ativo(request.getAtivo())
                .permiteVendaSemEstoque(request.getPermiteVendaSemEstoque())
                .atualizadoEm(LocalDateTime.now())
                .versao(1L)
                .build();

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(UUID empresaId, UUID id, ProdutoRequest request) {
        Produto produto = buscarEntidadeAtiva(empresaId, id);

        validarSku(request.getSku(), empresaId, id);
        validarCodigoBarras(request.getCodigoBarras(), empresaId, id);
        validarEstoqueMaximo(request.getEstoqueMinimo(), request.getEstoqueMaximo());

        produto.setNome(request.getNome());
        produto.setSku(normalizarTexto(request.getSku()));
        produto.setCodigoBarras(normalizarOpcional(request.getCodigoBarras()));
        produto.setDescricao(normalizarOpcional(request.getDescricao()));
        produto.setUnidadeMedida(normalizarTexto(request.getUnidadeMedida()));
        produto.setPrecoCusto(request.getPrecoCusto());
        produto.setPrecoVenda(request.getPrecoVenda());
        produto.setPrecoVendaAtacado(request.getPrecoVendaAtacado());
        produto.setEstoqueAtual(request.getEstoqueAtual());
        produto.setEstoqueMinimo(request.getEstoqueMinimo());
        produto.setEstoqueMaximo(request.getEstoqueMaximo());
        produto.setAtivo(request.getAtivo());
        produto.setPermiteVendaSemEstoque(request.getPermiteVendaSemEstoque());
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

    @Transactional(readOnly = true)
    public Produto buscarEntidadeInclusoDeletados(UUID empresaId, UUID id) {
        return produtoRepository.findByIdAndEmpresaId(id, empresaId).orElse(null);
    }

    @Transactional
    public ProdutoImagemResponse uploadImagem(UUID empresaId, UUID produtoId, MultipartFile arquivo) {
        Produto produto = buscarEntidadeAtiva(empresaId, produtoId);

        var upload = storageService.uploadProductImage(
                arquivo,
                empresaId.toString(),
                produtoId.toString()
        );

        produto.setImagemUrl(upload.url());
        produto.setImagemKey(upload.key());
        produto.setVersao(produto.getVersao() + 1);
        produto.setAtualizadoEm(LocalDateTime.now());

        Produto salvo = produtoRepository.save(produto);

        log.info("Imagem do produto {} atualizada na empresa {}", produtoId, empresaId);

        return new ProdutoImagemResponse(
                salvo.getId(),
                salvo.getImagemUrl(),
                salvo.getImagemKey()
        );
    }

    private void validarSku(String sku, UUID empresaId, UUID idExcluir) {
        String skuNormalizado = normalizarTexto(sku);

        boolean existe = (idExcluir == null)
                ? produtoRepository.existsBySkuAndEmpresaIdAndDeletadoEmIsNull(skuNormalizado, empresaId)
                : produtoRepository.existsSkuEmOutroProduto(empresaId, skuNormalizado, idExcluir);

        if (existe) {
            throw new RegraDeNegocioException("SKU '" + skuNormalizado + "' ja cadastrado nesta empresa.");
        }
    }

    private void validarCodigoBarras(String codigoBarras, UUID empresaId, UUID idExcluir) {
        String codigoNormalizado = normalizarOpcional(codigoBarras);
        if (codigoNormalizado == null) return;

        boolean existe = (idExcluir == null)
                ? produtoRepository.existsByCodigoBarrasAndEmpresaIdAndDeletadoEmIsNull(codigoNormalizado, empresaId)
                : produtoRepository.existsCodigoBarrasEmOutroProduto(empresaId, codigoNormalizado, idExcluir);

        if (existe) {
            throw new RegraDeNegocioException("Codigo de barras '" + codigoNormalizado + "' ja cadastrado nesta empresa.");
        }
    }

    private void validarEstoqueMaximo(java.math.BigDecimal estoqueMinimo, java.math.BigDecimal estoqueMaximo) {
        if (estoqueMaximo != null && estoqueMaximo.compareTo(estoqueMinimo) < 0) {
            throw new RegraDeNegocioException("O estoque maximo nao pode ser menor que o estoque minimo.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }
}
