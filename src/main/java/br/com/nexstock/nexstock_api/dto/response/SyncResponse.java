package br.com.nexstock.nexstock_api.dto.response;

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
public class SyncResponse {

    private UUID syncLogId;

    private LocalDateTime serverTimestamp;

    @Builder.Default
    private List<ProdutoResponse> produtosServidor = new ArrayList<>();

    private Integer produtosProcessados;
    private Integer movimentacoesRegistradas;

    @Builder.Default
    private List<ConflictInfo> conflitos = new ArrayList<>();

    private String status;
}
