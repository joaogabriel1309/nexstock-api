package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequest {
    @NotNull(message = "O ID é obrigatório")
    private UUID Id;

    @NotNull(message = "O ID da empresa é obrigatório")
    private UUID empresaId;
}
