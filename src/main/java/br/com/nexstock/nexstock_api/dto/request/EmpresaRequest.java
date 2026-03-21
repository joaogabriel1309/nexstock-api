package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaRequest {

    @NotNull(message = "ID do contrato é obrigatório")
    private UUID contratoId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200)
    private String nome;

    @NotBlank(message = "Razão social é obrigatória")
    @Size(max = 200)
    private String razaoSocial;

    @NotBlank(message = "CPF/CNPJ é obrigatório")
    @Size(max = 18)
    private String cpfCnpj;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String telefone;
}