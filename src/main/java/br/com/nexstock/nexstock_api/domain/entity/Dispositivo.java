package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "dispositivo",
    indexes = @Index(name = "idx_dispositivo_contrato_id", columnList = "contrato_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "sistema", nullable = false, length = 100)
    private String sistema;

    @Column(name = "ultimo_sync")
    private LocalDateTime ultimoSync;

    public void registrarSync() {
        this.ultimoSync = LocalDateTime.now();
    }

    public boolean nunca_sincronizou() {
        return this.ultimoSync == null;
    }
}
