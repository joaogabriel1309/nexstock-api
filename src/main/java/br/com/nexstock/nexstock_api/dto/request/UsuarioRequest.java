package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UsuarioRequest {
    @NotNull(message = "O ID é obrigatório")
    private UUID Id;

    @NotNull(message = "O ID da empresa é obrigatório")
    private UUID empresaId;
}
