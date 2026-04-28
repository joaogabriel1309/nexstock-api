package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoRequest {

    @NotNull(message = "O ID da empresa e obrigatorio")
    private UUID empresaId;

    @NotBlank(message = "O nome do produto e obrigatorio")
    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @NotBlank(message = "O SKU do produto e obrigatorio")
    @Size(max = 100, message = "O SKU deve ter no maximo 100 caracteres")
    private String sku;

    @Size(max = 100, message = "O codigo de barras deve ter no maximo 100 caracteres")
    private String codigoBarras;

    @Size(max = 5000, message = "A descricao deve ter no maximo 5000 caracteres")
    private String descricao;

    @NotBlank(message = "A unidade de medida e obrigatoria")
    @Size(max = 20, message = "A unidade de medida deve ter no maximo 20 caracteres")
    private String unidadeMedida;

    @NotNull(message = "O preco de custo e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "O preco de custo nao pode ser negativo")
    private BigDecimal precoCusto;

    @NotNull(message = "O preco de venda e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "O preco de venda nao pode ser negativo")
    private BigDecimal precoVenda;

    @DecimalMin(value = "0.0", inclusive = true, message = "O preco de venda no atacado nao pode ser negativo")
    private BigDecimal precoVendaAtacado;

    @NotNull(message = "O estoque atual e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "O estoque atual nao pode ser negativo")
    private BigDecimal estoqueAtual;

    @NotNull(message = "O estoque minimo e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "O estoque minimo nao pode ser negativo")
    private BigDecimal estoqueMinimo;

    @DecimalMin(value = "0.0", inclusive = true, message = "O estoque maximo nao pode ser negativo")
    private BigDecimal estoqueMaximo;

    @NotNull(message = "O status do produto e obrigatorio")
    private Boolean ativo;

    @NotNull(message = "A configuracao de venda sem estoque e obrigatoria")
    private Boolean permiteVendaSemEstoque;

    @AssertTrue(message = "O estoque maximo nao pode ser menor que o estoque minimo")
    public boolean isEstoqueMaximoValido() {
        return estoqueMaximo == null || estoqueMinimo == null || estoqueMaximo.compareTo(estoqueMinimo) >= 0;
    }
}
