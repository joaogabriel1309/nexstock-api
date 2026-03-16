package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plano")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "duracao_dias", nullable = false)
    private Integer duracaoDias;

    @Column(name = "max_dispositivos", nullable = false)
    @Builder.Default
    private Integer maxDispositivos = 1;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    public void desativar() {
        this.ativo = Boolean.FALSE;
    }
}
