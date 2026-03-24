package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "produto",
        indexes = {
                @Index(name = "idx_produto_empresa_id",      columnList = "empresa_id"),
                @Index(name = "idx_produto_atualizado",      columnList = "empresa_id, atualizado_em"),
                @Index(name = "idx_produto_codigo_barras",   columnList = "empresa_id, codigo_barras")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_produto_codbarra_empresa",
                columnNames = {"empresa_id", "codigo_barras"}
        )
)

@SQLDelete(sql = "UPDATE produto SET deletado_em = now(), atualizado_em = now() WHERE id = ?")
@SQLRestriction("deletado_em IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Column(name = "estoque", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal estoque = BigDecimal.ZERO;

    // Habilita o Optimistic Locking nativo do JPA
    @Version
    @Column(name = "versao", nullable = false)
    @Builder.Default
    private Long versao = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_ultima_alteracao")
    private Dispositivo dispositivoUltimaAlteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_ultima_alteracao")
    private Usuario usuarioUltimaAlteracao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public void registrarAtualizacao(Dispositivo dispositivo, Usuario usuario) {
        this.atualizadoEm = LocalDateTime.now();
        this.dispositivoUltimaAlteracao = dispositivo;
        this.usuarioUltimaAlteracao = usuario;
    }

    public void aplicarDadosSync(
            String nome,
            String codigoBarras,
            BigDecimal estoque,
            LocalDateTime atualizadoEm,
            Dispositivo dispositivo,
            Usuario usuario) {

        this.nome           = nome;
        this.codigoBarras   = codigoBarras;
        this.estoque        = estoque;
        this.atualizadoEm   = atualizadoEm;
        this.dispositivoUltimaAlteracao = dispositivo;
        this.usuarioUltimaAlteracao = usuario;
    }
}