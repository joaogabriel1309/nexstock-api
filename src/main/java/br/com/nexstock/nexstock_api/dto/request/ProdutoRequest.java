package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoRequest {

    @NotNull(message = "ID do contrato é obrigatório")
    private UUID contratoId;

    @NotNull(message = "ID do dispositivo é obrigatório")
    private UUID dispositivoId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String nome;

    @Size(max = 100)
    private String codigoBarras;

    @NotNull(message = "Estoque é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Estoque não pode ser negativo")
    private BigDecimal estoque;
}
