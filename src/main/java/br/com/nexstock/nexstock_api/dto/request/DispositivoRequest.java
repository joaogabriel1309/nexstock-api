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

    @NotNull(message = "O ID da empresa é obrigatório")
    private UUID empresaId;

    @NotNull(message = "O ID do usuário é obrigatório")
    private UUID usuarioId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "Sistema é obrigatório")
    @Size(max = 100, message = "O sistema deve ter no máximo 100 caracteres")
    private String sistema;
}