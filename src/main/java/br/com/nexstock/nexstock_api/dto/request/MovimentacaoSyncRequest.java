package br.com.nexstock.nexstock_api.dto.request;

import br.com.nexstock.nexstock_api.domain.enums.TipoMovimentacao;
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
public class MovimentacaoSyncRequest {

    @NotNull(message = "ID da movimentação é obrigatório")
    private UUID id;

    @NotNull(message = "ID do produto é obrigatório")
    private UUID produtoId;

    @NotNull(message = "Tipo é obrigatório")
    private TipoMovimentacao tipo;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotNull(message = "criadoEm é obrigatório")
    private LocalDateTime criadoEm;
}
