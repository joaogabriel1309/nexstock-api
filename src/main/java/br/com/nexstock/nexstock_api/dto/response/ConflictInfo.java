package br.com.nexstock.nexstock_api.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictInfo {

    private UUID produtoId;

    private String resolucao;

    private LocalDateTime clienteAtualizadoEm;
    private LocalDateTime servidorAtualizadoEm;
    private Long versaoFinal;
}
