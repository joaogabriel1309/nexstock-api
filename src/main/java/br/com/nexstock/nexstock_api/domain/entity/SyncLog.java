package br.com.nexstock.nexstock_api.domain.entity;

import br.com.nexstock.nexstock_api.domain.enums.StatusSync;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "sync_log",
    indexes = {
        @Index(name = "idx_sync_log_contrato_id",    columnList = "contrato_id"),
        @Index(name = "idx_sync_log_dispositivo_id", columnList = "dispositivo_id"),
        @Index(name = "idx_sync_log_data_sync",      columnList = "contrato_id, data_sync")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "data_sync", nullable = false)
    private LocalDateTime dataSync;

    @Column(name = "registros_enviados", nullable = false)
    @Builder.Default
    private Integer registrosEnviados = 0;

    @Column(name = "registros_recebidos", nullable = false)
    @Builder.Default
    private Integer registrosRecebidos = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StatusSync status;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;
}
