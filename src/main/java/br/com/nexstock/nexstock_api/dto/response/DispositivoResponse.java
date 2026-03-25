package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoResponse {

    private UUID id;
    private UUID empresaId;
    private String nome;
    private String sistema;
    private LocalDateTime ultimoSync;

    public static DispositivoResponse from(Dispositivo dispositivo) {
        return DispositivoResponse.builder()
                .id(dispositivo.getId())
                .empresaId(dispositivo.getEmpresa().getId())
                .nome(dispositivo.getNome())
                .sistema(dispositivo.getSistema())
                .ultimoSync(dispositivo.getUltimoSync())
                .build();
    }
}