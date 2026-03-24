package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.dto.request.DispositivoRequest;
import br.com.nexstock.nexstock_api.dto.response.DispositivoResponse;
import br.com.nexstock.nexstock_api.exception.LimiteDispositivosException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.DispositivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;
    private final ContratoService       contratoService;

    @Transactional
    public DispositivoResponse registrar(DispositivoRequest request) {
        Contrato contrato = contratoService.buscarEntidadeVigente(request.getContratoId());

        long totalAtual = dispositivoRepository.countByContratoId(contrato.getId());
        int  limite     = contrato.getPlano().getMaxDispositivos();

        if (totalAtual >= limite) {
            throw new LimiteDispositivosException(limite, contrato.getPlano().getNome());
        }

        Dispositivo dispositivo = Dispositivo.builder()
                .nome(request.getNome())
                .sistema(request.getSistema())
                .build();

        Dispositivo salvo = dispositivoRepository.save(dispositivo);
        log.info("Dispositivo {} registrado no contrato {}", salvo.getId(), contrato.getId());
        return DispositivoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public DispositivoResponse buscarPorId(UUID contratoId, UUID id) {
        return DispositivoResponse.from(buscarEntidade(contratoId, id));
    }

    @Transactional(readOnly = true)
    public List<DispositivoResponse> listarPorContrato(UUID contratoId) {
        return dispositivoRepository.findAllByContratoId(contratoId)
                .stream()
                .map(DispositivoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Dispositivo buscarEntidade(UUID contratoId, UUID id) {
        return dispositivoRepository.findByIdAndContratoId(id, contratoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Dispositivo", id));
    }
}
