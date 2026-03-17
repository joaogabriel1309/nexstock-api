package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String nome;

    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo")
    private BigDecimal preco;

    @NotNull(message = "Duração em dias é obrigatória")
    @Min(value = 1, message = "Duração mínima é 1 dia")
    private Integer duracaoDias;

    @NotNull(message = "Limite de dispositivos é obrigatório")
    @Min(value = 1, message = "Mínimo de 1 dispositivo")
    private Integer maxDispositivos;
}
