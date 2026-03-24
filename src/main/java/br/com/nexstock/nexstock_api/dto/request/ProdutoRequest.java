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

    @NotNull(message = "O ID da empresa é obrigatório")
    private UUID empresaId;

    @NotNull(message = "O ID do dispositivo é obrigatório")
    private UUID dispositivoId;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
    private String nome;

    @Size(max = 100, message = "O código de barras deve ter no máximo 100 caracteres")
    private String codigoBarras;

    @NotNull(message = "O estoque inicial é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O estoque não pode ser negativo")
    private BigDecimal estoque;
}