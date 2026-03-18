package br.com.nexstock.nexstock_api.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private UUID usuarioId;
    private UUID contratoId;
    private String nome;
    private String email;
    private String role;
    private long expiracaoEmMs;
}