package br.com.nexstock.nexstock_api.dto.response;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoResponse {

    private UUID id;
    private UUID empresaId;
    private String empresaNome;
    private UUID planoId;
    private String planoNome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusContrato status;
    private UUID renovadoDeId;
    private LocalDateTime criadoEm;

    public static ContratoResponse from(Contrato contrato) {
        return ContratoResponse.builder()
                .id(contrato.getId())
                .empresaId(contrato.getEmpresa() != null ? contrato.getEmpresa().getId() : null)
                .planoId(contrato.getPlano() != null ? contrato.getPlano().getId() : null)
                .planoNome(contrato.getPlano().getNome())
                .dataInicio(contrato.getDataInicio())
                .dataFim(contrato.getDataFim())
                .status(contrato.getStatus())
                .renovadoDeId(contrato.getRenovadoDe() != null ? contrato.getRenovadoDe().getId() : null)
                .criadoEm(contrato.getCriadoEm())
                .build();
    }
}