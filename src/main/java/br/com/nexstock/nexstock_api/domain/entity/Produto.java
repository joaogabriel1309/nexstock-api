package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "produto",
    indexes = {
        @Index(name = "idx_produto_contrato_id",   columnList = "contrato_id"),
        @Index(name = "idx_produto_sync",           columnList = "contrato_id, atualizado_em"),
        @Index(name = "idx_produto_deletado",       columnList = "contrato_id, deletado"),
        @Index(name = "idx_produto_codigo_barras",  columnList = "contrato_id, codigo_barras")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_produto_codbarra_contrato",
        columnNames = {"contrato_id", "codigo_barras"}
    )
)
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
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Column(name = "estoque", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal estoque = BigDecimal.ZERO;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "versao", nullable = false)
    @Builder.Default
    private Long versao = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_ultima_alteracao")
    private Dispositivo dispositivoUltimaAlteracao;

    @Column(name = "deletado", nullable = false)
    @Builder.Default
    private Boolean deletado = Boolean.FALSE;

    public void marcarComoDeletado(Dispositivo dispositivo) {
        this.deletado       = Boolean.TRUE;
        this.versao         = this.versao + 1;
        this.atualizadoEm   = LocalDateTime.now();
        this.dispositivoUltimaAlteracao = dispositivo;
    }

    public void registrarAtualizacao(Dispositivo dispositivo) {
        this.versao         = this.versao + 1;
        this.atualizadoEm   = LocalDateTime.now();
        this.dispositivoUltimaAlteracao = dispositivo;
    }

    public void aplicarDadosSync(
            String nome,
            String codigoBarras,
            BigDecimal estoque,
            LocalDateTime atualizadoEm,
            Boolean deletado,
            Dispositivo dispositivo) {

        this.nome           = nome;
        this.codigoBarras   = codigoBarras;
        this.estoque        = estoque;
        this.atualizadoEm   = atualizadoEm;
        this.deletado       = deletado != null ? deletado : Boolean.FALSE;
        this.versao         = this.versao + 1;
        this.dispositivoUltimaAlteracao = dispositivo;
    }
}
