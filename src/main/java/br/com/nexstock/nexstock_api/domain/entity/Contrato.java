package br.com.nexstock.nexstock_api.domain.entity;

import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "contrato",
    indexes = {
        @Index(name = "idx_contrato_cliente_id", columnList = "cliente_id"),
        @Index(name = "idx_contrato_status",     columnList = "status"),
        @Index(name = "idx_contrato_data_fim",   columnList = "data_fim")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private StatusContrato status = StatusContrato.ATIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renovado_de")
    private Contrato renovadoDe;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    public boolean isAtivo() {
        return StatusContrato.ATIVO.equals(this.status);
    }

    public boolean estaVigente() {
        return isAtivo() && !LocalDate.now().isAfter(this.dataFim);
    }

    public void expirar() {
        this.status = StatusContrato.EXPIRADO;
    }

    public void cancelar() {
        this.status = StatusContrato.CANCELADO;
    }

    public void suspender() {
        this.status = StatusContrato.SUSPENSO;
    }

    public void reativar() {
        this.status = StatusContrato.ATIVO;
    }
}
