package br.com.nexstock.nexstock_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "empresa",
        indexes = {
                @Index(name = "idx_empresa_contrato_id",  columnList = "contrato_id"),
                @Index(name = "idx_empresa_nome",         columnList = "nome"),
                @Index(name = "idx_empresa_cpf_cnpj",     columnList = "cpf_cnpj"),
                @Index(name = "idx_empresa_atualizado",   columnList = "atualizado_em")
        }
)
@SQLDelete(sql = "UPDATE empresa SET deletado_em = now(), atualizado_em = now() WHERE id = ?")
@SQLRestriction("deletado_em IS NULL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "cpf_cnpj", nullable = false, length = 18, unique = true)
    private String cpfCnpj;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

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

    public void desativar() {
        this.ativo = false;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void reativar() {
        this.ativo = true;
        this.atualizadoEm = LocalDateTime.now();
    }
}