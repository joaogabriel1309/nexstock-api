package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoRequest {

    @NotNull(message = "ID do cliente é obrigatório")
    private UUID clienteId;

    @NotNull(message = "ID do plano é obrigatório")
    private UUID planoId;
}
