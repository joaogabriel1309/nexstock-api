package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.StatusContrato;
import br.com.nexstock.nexstock_api.dto.request.ContratoRequest;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.ContratoRepository;
import br.com.nexstock.nexstock_api.repository.PlanoRepository;
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
    private final PlanoRepository planoRepository;

    @Transactional
    public Contrato gerarContratoInicial(UUID planoId) {
        Plano plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", planoId));

        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new RegraDeNegocioException("O plano '" + plano.getNome() + "' não está disponível para novas contratações.");
        }

        LocalDate inicio = LocalDate.now();
        LocalDate fim    = inicio.plusDays(plano.getDuracaoDias());

        Contrato contrato = Contrato.builder()
                .plano(plano)
                .dataInicio(inicio)
                .dataFim(fim)
                .status(StatusContrato.ATIVO)
                .build();

        log.info("Gerando contrato inicial: Plano {} | Vigência: {} até {}", plano.getNome(), inicio, fim);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public Contrato renovarInternamente(UUID contratoAtualId) {
        Contrato atual = buscarEntidade(contratoAtualId);

        if (StatusContrato.CANCELADO.equals(atual.getStatus())) {
            throw new RegraDeNegocioException("Contratos cancelados não podem ser renovados.");
        }

        atual.expirar();
        contratoRepository.save(atual);

        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusDays(atual.getPlano().getDuracaoDias());

        Contrato novo = Contrato.builder()
                .plano(atual.getPlano())
                .dataInicio(inicio)
                .dataFim(fim)
                .status(StatusContrato.ATIVO)
                .renovadoDe(atual)
                .build();

        log.info("Contrato {} renovado automaticamente para o novo ID {}", contratoAtualId, novo.getId());

        return contratoRepository.save(novo);
    }

    @Transactional
    public void cancelar(UUID id) {
        Contrato contrato = buscarEntidade(id);
        contrato.cancelar();
        contratoRepository.save(contrato);
    }

    @Scheduled(cron = "0 0 0 * * *") // Roda todo dia à meia-noite
    @Transactional
    public void expirarContratosVencidos() {
        List<Contrato> vencidos = contratoRepository.findContratosVencidos(LocalDate.now());
        vencidos.forEach(Contrato::expirar);
        contratoRepository.saveAll(vencidos);

        if (!vencidos.isEmpty()) {
            log.info("Processamento diário: {} contratos movidos para expirado.", vencidos.size());
        }
    }

    @Transactional(readOnly = true)
    public Contrato buscarEntidade(UUID id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
    }
}