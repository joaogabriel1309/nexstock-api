package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoRequest {

    @NotNull(message = "ID do contrato é obrigatório")
    private UUID contratoId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "Sistema é obrigatório")
    @Size(max = 100)
    private String sistema;
}
