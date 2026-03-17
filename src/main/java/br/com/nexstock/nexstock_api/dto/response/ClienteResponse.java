package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Cliente;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {

    private UUID id;
    private String nome;
    private String email;
    private String documento;
    private String telefone;
    private LocalDateTime criadoEm;
    private Boolean ativo;

    public static ClienteResponse from(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .documento(cliente.getDocumento())
                .telefone(cliente.getTelefone())
                .criadoEm(cliente.getCriadoEm())
                .ativo(cliente.getAtivo())
                .build();
    }
}
