package br.com.nexstock.nexstock_api.domain.entity;

import br.com.nexstock.nexstock_api.domain.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "movimentacao_estoque",
    indexes = {
        @Index(name = "idx_mov_contrato_id",    columnList = "contrato_id"),
        @Index(name = "idx_mov_produto_id",     columnList = "produto_id"),
        @Index(name = "idx_mov_criado_em",      columnList = "contrato_id, criado_em"),
        @Index(name = "idx_mov_dispositivo_id", columnList = "dispositivo_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;
}
