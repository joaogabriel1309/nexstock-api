package br.com.nexstock.nexstock_api.repository;

import br.com.nexstock.nexstock_api.IntegrationTestBase;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProdutoRepository")
class ProdutoRepositoryIT extends IntegrationTestBase {

    @Autowired ProdutoRepository produtoRepository;
    @Autowired EmpresaRepository empresaRepository;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        limparBanco();

        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Repo")
                .razaoSocial("Empresa Repo LTDA")
                .cpfCnpj("99999999000199")
                .ativo(true)
                .build());
    }

    @Test
    @DisplayName("deve listar apenas produtos ativos da empresa")
    void deveListarApenasProdutosAtivosDaEmpresa() {
        Produto ativo = produtoRepository.save(produto("Produto ativo", "SKU-ATIVO", "7890000000001", null));
        Produto deletado = produtoRepository.save(produto("Produto removido", "SKU-REMOVIDO", "7890000000002", LocalDateTime.now()));

        var produtos = produtoRepository.findAllByEmpresaIdAndDeletadoEmIsNull(empresa.getId());

        assertThat(produtos).extracting(Produto::getId).contains(ativo.getId());
        assertThat(produtos).extracting(Produto::getId).doesNotContain(deletado.getId());
    }

    @Test
    @DisplayName("deve identificar codigo de barras duplicado em outro produto")
    void deveIdentificarCodigoBarrasDuplicadoEmOutroProduto() {
        Produto produtoA = produtoRepository.save(produto("Produto A", "SKU-A", "7890000000001", null));
        Produto produtoB = produtoRepository.save(produto("Produto B", "SKU-B", "7890000000002", null));

        boolean duplicado = produtoRepository.existsCodigoBarrasEmOutroProduto(
                empresa.getId(), produtoB.getCodigoBarras(), produtoA.getId());

        assertThat(duplicado).isTrue();
    }

    @Test
    @DisplayName("deve identificar sku duplicado em outro produto")
    void deveIdentificarSkuDuplicadoEmOutroProduto() {
        Produto produtoA = produtoRepository.save(produto("Produto A", "SKU-A", "7890000000001", null));
        produtoRepository.save(produto("Produto B", "SKU-B", "7890000000002", null));

        boolean duplicado = produtoRepository.existsSkuEmOutroProduto(
                empresa.getId(), "SKU-B", produtoA.getId());

        assertThat(duplicado).isTrue();
    }

    private Produto produto(String nome, String sku, String codigoBarras, LocalDateTime deletadoEm) {
        return Produto.builder()
                .empresa(empresa)
                .nome(nome)
                .sku(sku)
                .codigoBarras(codigoBarras)
                .descricao("Descricao " + nome)
                .unidadeMedida("UN")
                .precoCusto(BigDecimal.ONE)
                .precoVenda(BigDecimal.TEN)
                .estoqueAtual(BigDecimal.ONE)
                .estoqueMinimo(BigDecimal.ZERO)
                .ativo(true)
                .permiteVendaSemEstoque(false)
                .versao(1L)
                .deletadoEm(deletadoEm)
                .build();
    }
}
