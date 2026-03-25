package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoSyncRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private UUID id;

    @NotNull(message = "ID da empresa é obrigatório para sincronização")
    private UUID empresaId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String nome;

    @Size(max = 100)
    private String codigoBarras;

    @NotNull(message = "Estoque é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estoque;

    @NotNull(message = "atualizadoEm é obrigatório")
    private LocalDateTime atualizadoEm;

    @NotNull(message = "versão é obrigatória para controle de concorrência otimista")
    private Long versao;

    private LocalDateTime deletadoEm;
}