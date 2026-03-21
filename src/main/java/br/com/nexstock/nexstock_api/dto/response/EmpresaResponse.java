package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {

    private UUID id;
    private UUID contratoId;
    private String nome;
    private String razaoSocial;
    private String cpfCnpj;
    private String email;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    public static EmpresaResponse from(Empresa empresa) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .contratoId(empresa.getContrato().getId())
                .nome(empresa.getNome())
                .razaoSocial(empresa.getRazaoSocial())
                .cpfCnpj(empresa.getCpfCnpj())
                .email(empresa.getEmail())
                .telefone(empresa.getTelefone())
                .ativo(empresa.getAtivo())
                .criadoEm(empresa.getCriadoEm())
                .build();
    }
}