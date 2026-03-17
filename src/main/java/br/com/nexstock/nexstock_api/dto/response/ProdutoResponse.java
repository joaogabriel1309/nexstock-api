package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Produto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoResponse {

    private UUID id;
    private UUID contratoId;
    private String nome;
    private String codigoBarras;
    private BigDecimal estoque;
    private LocalDateTime atualizadoEm;
    private Long versao;
    private UUID dispositivoUltimaAlteracaoId;
    private Boolean deletado;

    public static ProdutoResponse from(Produto produto) {
        return ProdutoResponse.builder()
                .id(produto.getId())
                .contratoId(produto.getContrato().getId())
                .nome(produto.getNome())
                .codigoBarras(produto.getCodigoBarras())
                .estoque(produto.getEstoque())
                .atualizadoEm(produto.getAtualizadoEm())
                .versao(produto.getVersao())
                .dispositivoUltimaAlteracaoId(
                    produto.getDispositivoUltimaAlteracao() != null
                        ? produto.getDispositivoUltimaAlteracao().getId()
                        : null
                )
                .deletado(produto.getDeletado())
                .build();
    }
}
