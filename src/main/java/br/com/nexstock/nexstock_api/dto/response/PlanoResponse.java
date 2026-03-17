package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Plano;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer duracaoDias;
    private Integer maxDispositivos;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    public static PlanoResponse from(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .descricao(plano.getDescricao())
                .preco(plano.getPreco())
                .duracaoDias(plano.getDuracaoDias())
                .maxDispositivos(plano.getMaxDispositivos())
                .ativo(plano.getAtivo())
                .criadoEm(plano.getCriadoEm())
                .build();
    }
}
