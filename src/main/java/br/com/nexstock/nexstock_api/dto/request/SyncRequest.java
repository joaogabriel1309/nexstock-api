package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRequest {

    @NotNull(message = "O ID da empresa e obrigatorio")
    private UUID empresaId;

    private LocalDateTime ultimoSyncCliente;

    @Valid
    @Builder.Default
    private List<ProdutoSyncRequest> produtos = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<MovimentacaoSyncRequest> movimentacoes = new ArrayList<>();
}
