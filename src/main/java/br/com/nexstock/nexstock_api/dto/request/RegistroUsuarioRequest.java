package br.com.nexstock.nexstock_api.dto.request;

import br.com.nexstock.nexstock_api.domain.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroUsuarioRequest {

    @NotNull(message = "ID do contrato é obrigatório")
    private UUID contratoId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;

    private Role role;
}