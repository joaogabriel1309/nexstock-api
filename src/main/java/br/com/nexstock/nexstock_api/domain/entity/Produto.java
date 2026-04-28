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
                @Index(name = "idx_produto_codigo_barras",   columnList = "empresa_id, codigo_barras"),
                @Index(name = "idx_produto_sku",             columnList = "empresa_id, sku")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_produto_codbarra_empresa",
                        columnNames = {"empresa_id", "codigo_barras"}
                ),
                @UniqueConstraint(
                        name = "uq_produto_sku_empresa",
                        columnNames = {"empresa_id", "sku"}
                )
        }
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

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    @Column(name = "preco_custo", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal precoCusto = BigDecimal.ZERO;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal precoVenda = BigDecimal.ZERO;

    @Column(name = "preco_venda_atacado", precision = 15, scale = 4)
    private BigDecimal precoVendaAtacado;

    @Column(name = "estoque", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(name = "estoque_maximo", precision = 15, scale = 4)
    private BigDecimal estoqueMaximo;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "permite_venda_sem_estoque", nullable = false)
    @Builder.Default
    private Boolean permiteVendaSemEstoque = Boolean.FALSE;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(name = "imagem_key", length = 500)
    private String imagemKey;

    // Habilita o Optimistic Locking nativo do JPA
    @Version
    @Column(name = "versao", nullable = false)
    @Builder.Default
    private Long versao = 1L;

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

    public void registrarAtualizacao(Usuario usuario) {
        this.atualizadoEm = LocalDateTime.now();
        this.usuarioUltimaAlteracao = usuario;
    }

    public void aplicarDadosSync(
            String nome,
            String sku,
            String codigoBarras,
            String descricao,
            String unidadeMedida,
            BigDecimal precoCusto,
            BigDecimal precoVenda,
            BigDecimal precoVendaAtacado,
            BigDecimal estoqueAtual,
            BigDecimal estoqueMinimo,
            BigDecimal estoqueMaximo,
            Boolean ativo,
            Boolean permiteVendaSemEstoque,
            LocalDateTime atualizadoEm,
            Usuario usuario) {

        this.nome = nome;
        this.sku = sku;
        this.codigoBarras = codigoBarras;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.precoVendaAtacado = precoVendaAtacado;
        this.estoqueAtual = estoqueAtual;
        this.estoqueMinimo = estoqueMinimo;
        this.estoqueMaximo = estoqueMaximo;
        this.ativo = ativo;
        this.permiteVendaSemEstoque = permiteVendaSemEstoque;
        this.atualizadoEm = atualizadoEm;
        this.usuarioUltimaAlteracao = usuario;
    }
}
