package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

    @NotNull(message = "ID do contrato é obrigatório")
    private UUID contratoId;

    @NotNull(message = "ID do dispositivo é obrigatório")
    private UUID dispositivoId;

    private LocalDateTime ultimoSyncCliente;

//    @Valid
//    @Builder.Default
//    private List<ProdutoSyncRequest> produtos = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<MovimentacaoSyncRequest> movimentacoes = new ArrayList<>();
}
