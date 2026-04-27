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
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoSyncRequest {

    @NotNull(message = "ID do produto e obrigatorio")
    private UUID id;

    @NotNull(message = "ID da empresa e obrigatorio para sincronizacao")
    private UUID empresaId;

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 255)
    private String nome;

    @NotBlank(message = "SKU e obrigatorio")
    @Size(max = 100)
    private String sku;

    @Size(max = 100)
    private String codigoBarras;

    @Size(max = 5000)
    private String descricao;

    @NotBlank(message = "Unidade de medida e obrigatoria")
    @Size(max = 20)
    private String unidadeMedida;

    @NotNull(message = "Preco de custo e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal precoCusto;

    @NotNull(message = "Preco de venda e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal precoVenda;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal precoVendaAtacado;

    @NotNull(message = "Estoque atual e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estoqueAtual;

    @NotNull(message = "Estoque minimo e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estoqueMinimo;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estoqueMaximo;

    @NotNull(message = "Ativo e obrigatorio")
    private Boolean ativo;

    @NotNull(message = "Permite venda sem estoque e obrigatorio")
    private Boolean permiteVendaSemEstoque;

    @NotNull(message = "atualizadoEm e obrigatorio")
    private LocalDateTime atualizadoEm;

    @NotNull(message = "versao e obrigatoria para controle de concorrencia otimista")
    private Long versao;

    private LocalDateTime deletadoEm;

    @AssertTrue(message = "O estoque maximo nao pode ser menor que o estoque minimo")
    public boolean isEstoqueMaximoValido() {
        return estoqueMaximo == null || estoqueMinimo == null || estoqueMaximo.compareTo(estoqueMinimo) >= 0;
    }
}
