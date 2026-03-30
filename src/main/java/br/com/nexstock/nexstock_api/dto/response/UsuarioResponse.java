package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private UUID id;
    private UUID empresaId;
    private String nome;
    private String email;
    private Role role;

    private static UsuarioResponse from(Usuario usuario){
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .empresaId(usuario.getEmpresa().getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole())
                .build();
    }
}
