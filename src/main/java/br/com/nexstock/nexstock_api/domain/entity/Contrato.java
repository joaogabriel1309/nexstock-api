package br.com.nexstock.nexstock_api.domain.entity;

import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "contrato",
        indexes = {
                @Index(name = "idx_contrato_empresa_id", columnList = "empresa_id"),
                @Index(name = "idx_contrato_plano_id",   columnList = "plano_id"),
                @Index(name = "idx_contrato_status",     columnList = "status"),
                @Index(name = "idx_contrato_atualizado", columnList = "atualizado_em")
        }
)

@SQLDelete(sql = "UPDATE Contrato SET deletado_em = now(), atualizado_em = now() WHERE id = ?")
@SQLRestriction("deletado_em IS NULL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(mappedBy = "contrato")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusContrato status = StatusContrato.ATIVO;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renovado_de")
    private Contrato renovadoDe;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean estaVigente() {
        return StatusContrato.ATIVO.equals(this.status) &&
                !LocalDate.now().isAfter(this.dataFim);
    }

    public void cancelar()  { this.status = StatusContrato.CANCELADO; }
    public void suspender() { this.status = StatusContrato.SUSPENSO;  }
    public void reativar()  { this.status = StatusContrato.ATIVO;     }
    public void expirar()   { this.status = StatusContrato.EXPIRADO;  }
}