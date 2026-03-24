package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.request.ContratoRequest;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ClienteService     clienteService;
    private final PlanoService       planoService;

    @Transactional
    public ContratoResponse contratar(ContratoRequest request) {
        Cliente cliente = clienteService.buscarEntidade(request.getClienteId());
        Plano   plano   = planoService.buscarEntidade(request.getPlanoId());

        if (!plano.getAtivo()) {
            throw new RegraDeNegocioException("Plano '" + plano.getNome() + "' não está disponível.");
        }

        contratoRepository
            .findByClienteIdAndStatus(cliente.getId(), StatusContrato.ATIVO)
            .ifPresent(c -> { throw new RegraDeNegocioException(
                "Cliente já possui um contrato ativo (id: " + c.getId() + "). Cancele-o antes de contratar novamente."
            ); });

        LocalDate inicio = LocalDate.now();
        LocalDate fim    = inicio.plusDays(plano.getDuracaoDias());

        Contrato contrato = Contrato.builder()
                .plano(plano)
                .dataInicio(inicio)
                .dataFim(fim)
                .build();

        Contrato salvo = contratoRepository.save(contrato);
        log.info("Contrato {} criado para cliente {} — vigência até {}", salvo.getId(), cliente.getId(), fim);
        return ContratoResponse.from(salvo);
    }

    @Transactional
    public ContratoResponse renovar(UUID contratoAtualId) {
        Contrato atual = buscarEntidade(contratoAtualId);

        if (StatusContrato.CANCELADO.equals(atual.getStatus())) {
            throw new RegraDeNegocioException("Contratos cancelados não podem ser renovados.");
        }

        atual.expirar();
        contratoRepository.save(atual);

        LocalDate inicio = LocalDate.now();
        LocalDate fim    = inicio.plusDays(atual.getPlano().getDuracaoDias());

        Contrato novo = Contrato.builder()
                .plano(atual.getPlano())
                .dataInicio(inicio)
                .dataFim(fim)
                .renovadoDe(atual)
                .build();

        Contrato salvo = contratoRepository.save(novo);
        log.info("Contrato {} renovado → novo contrato {}", contratoAtualId, salvo.getId());
        return ContratoResponse.from(salvo);
    }

    @Transactional
    public void cancelar(UUID id) {
        Contrato contrato = buscarEntidade(id);
        contrato.cancelar();
        contratoRepository.save(contrato);
        log.info("Contrato {} cancelado", id);
    }

    @Transactional
    public void suspender(UUID id) {
        Contrato contrato = buscarEntidade(id);
        contrato.suspender();
        contratoRepository.save(contrato);
        log.info("Contrato {} suspenso", id);
    }

    @Transactional
    public void reativar(UUID id) {
        Contrato contrato = buscarEntidade(id);
        contrato.reativar();
        contratoRepository.save(contrato);
        log.info("Contrato {} reativado", id);
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscarPorId(UUID id) {
        return ContratoResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> listarPorCliente(UUID clienteId) {
        return contratoRepository.findAllByClienteIdOrderByCriadoEmDesc(clienteId)
                .stream()
                .map(ContratoResponse::from)
                .toList();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirarContratosVencidos() {
        List<Contrato> vencidos = contratoRepository.findContratosVencidos(LocalDate.now());
        vencidos.forEach(Contrato::expirar);
        contratoRepository.saveAll(vencidos);

        if (!vencidos.isEmpty()) {
            log.info("Job de expiração: {} contrato(s) expirado(s)", vencidos.size());
        }
    }

    @Transactional(readOnly = true)
    public Contrato buscarEntidade(UUID id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
    }

    @Transactional(readOnly = true)
    public Contrato buscarEntidadeVigente(UUID id) {
        Contrato contrato = buscarEntidade(id);
        if (!contrato.estaVigente()) {
            throw new br.com.nexstock.nexstock_api.exception.ContratoInativoException(contrato.getStatus().name());
        }
        return contrato;
    }
}
