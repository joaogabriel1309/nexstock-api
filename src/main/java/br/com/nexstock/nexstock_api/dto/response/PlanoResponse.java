package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Plano;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoResponse {

    private UUID id;
    private String nome;
    private BigDecimal preco;
    private Integer duracaoDias;
    private Integer maxDispositivos;
    private Boolean ativo;

    public static PlanoResponse from(Plano plano) {
        return PlanoResponse.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .preco(plano.getPreco())
                .duracaoDias(plano.getDuracaoDias())
                .maxDispositivos(plano.getMaxDispositivos())
                .ativo(plano.getAtivo())
                .build();
    }
}