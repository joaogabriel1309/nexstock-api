package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Produto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoResponse {

    private UUID id;
    private UUID empresaId;
    private String nome;
    private String sku;
    private String codigoBarras;
    private String descricao;
    private String unidadeMedida;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal precoVendaAtacado;
    private BigDecimal estoqueAtual;
    private BigDecimal estoqueMinimo;
    private BigDecimal estoqueMaximo;
    private Boolean ativo;
    private Boolean permiteVendaSemEstoque;
    private BigDecimal margemValor;
    private BigDecimal margemPercentual;
    private String statusEstoque;
    private String imagemUrl;
    private String imagemKey;
    private LocalDateTime atualizadoEm;
    private Long versao;
    private LocalDateTime deletadoEm;

    public static ProdutoResponse from(Produto produto) {
        if (produto == null) return null;

        BigDecimal margemValor = produto.getPrecoVenda().subtract(produto.getPrecoCusto());
        BigDecimal margemPercentual = calcularMargemPercentual(produto.getPrecoCusto(), margemValor);

        return ProdutoResponse.builder()
                .id(produto.getId())
                .empresaId(produto.getEmpresa().getId())
                .nome(produto.getNome())
                .sku(produto.getSku())
                .codigoBarras(produto.getCodigoBarras())
                .descricao(produto.getDescricao())
                .unidadeMedida(produto.getUnidadeMedida())
                .precoCusto(produto.getPrecoCusto())
                .precoVenda(produto.getPrecoVenda())
                .precoVendaAtacado(produto.getPrecoVendaAtacado())
                .estoqueAtual(produto.getEstoqueAtual())
                .estoqueMinimo(produto.getEstoqueMinimo())
                .estoqueMaximo(produto.getEstoqueMaximo())
                .ativo(produto.getAtivo())
                .permiteVendaSemEstoque(produto.getPermiteVendaSemEstoque())
                .margemValor(margemValor)
                .margemPercentual(margemPercentual)
                .statusEstoque(determinarStatusEstoque(produto))
                .imagemUrl(produto.getImagemUrl())
                .imagemKey(produto.getImagemKey())
                .atualizadoEm(produto.getAtualizadoEm())
                .versao(produto.getVersao())
                .deletadoEm(produto.getDeletadoEm())
                .build();
    }

    private static BigDecimal calcularMargemPercentual(BigDecimal precoCusto, BigDecimal margemValor) {
        if (precoCusto == null || BigDecimal.ZERO.compareTo(precoCusto) == 0) {
            return null;
        }

        return margemValor
                .multiply(BigDecimal.valueOf(100))
                .divide(precoCusto, 2, RoundingMode.HALF_UP);
    }

    private static String determinarStatusEstoque(Produto produto) {
        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            return "INATIVO";
        }

        if (produto.getEstoqueAtual().compareTo(produto.getEstoqueMinimo()) < 0) {
            return produto.getEstoqueAtual().compareTo(BigDecimal.ZERO) == 0
                    ? "EM_RUPTURA"
                    : "ESTOQUE_BAIXO";
        }

        if (produto.getEstoqueMaximo() != null && produto.getEstoqueAtual().compareTo(produto.getEstoqueMaximo()) > 0) {
            return "ESTOQUE_EXCEDENTE";
        }

        return "NORMAL";
    }
}
