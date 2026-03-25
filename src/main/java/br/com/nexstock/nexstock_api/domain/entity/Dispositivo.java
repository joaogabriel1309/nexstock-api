package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "dispositivo",
        indexes = {
                @Index(name = "idx_dispositivo_empresa_id", columnList = "empresa_id"),
                @Index(name = "idx_dispositivo_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_dispositivo_atualizado", columnList = "atualizado_em")
        }
)
@SQLDelete(sql = "UPDATE dispositivo SET deletado_em = now(), atualizado_em = now() WHERE id = ?")
@SQLRestriction("deletado_em IS NULL")

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
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "sistema", nullable = false, length = 100)
    private String sistema;

    @Column(name = "ultimo_sync")
    private LocalDateTime ultimoSync;

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

    public void registrarSync() {
        this.ultimoSync = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }
}