package br.com.nexstock.nexstock_api.dto.request;

import jakarta.validation.constraints.Email;
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
public class SetupRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200)
    private String empresaNome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Por favor, insira um e-mail válido")
    private String empresaEmail;

    @NotBlank(message = "CPF/CNPJ é obrigatória")
    @Size(max = 18)
    private String empresaCpfCnpj;

    @Size(max = 20)
    private String empresaTelefone;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150)
    private String adminNome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Por favor, insira um e-mail válido")
    private String adminEmail;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String adminSenha;

    @NotNull(message = "Plano é obrigatório")
    private UUID planoId;
}
