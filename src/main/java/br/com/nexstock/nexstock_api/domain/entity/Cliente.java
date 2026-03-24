package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

//@Entity
//@Table(
//    name = "cliente",
//    uniqueConstraints = @UniqueConstraint(
//        name = "uq_cliente_email",
//        columnNames = "email"
//    )
//)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = Boolean.TRUE;

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    public void desativar() {
        this.ativo = Boolean.FALSE;
    }

    public void reativar() {
        this.ativo = Boolean.TRUE;
    }
}
