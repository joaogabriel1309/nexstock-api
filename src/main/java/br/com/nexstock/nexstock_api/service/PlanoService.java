package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Plano;
import br.com.nexstock.nexstock_api.dto.request.PlanoRequest;
import br.com.nexstock.nexstock_api.dto.response.PlanoResponse;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanoService {

    private final PlanoRepository planoRepository;

    @Transactional
    public PlanoResponse criar(PlanoRequest request) {
        log.info("Criando plano: {}", request.getNome());

        Plano plano = Plano.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .preco(request.getPreco())
                .duracaoDias(request.getDuracaoDias())
                .maxDispositivos(request.getMaxDispositivos())
                .build();

        return PlanoResponse.from(planoRepository.save(plano));
    }

    @Transactional
    public PlanoResponse atualizar(UUID id, PlanoRequest request) {
        Plano plano = buscarEntidade(id);

        plano.setNome(request.getNome());
        plano.setDescricao(request.getDescricao());
        plano.setPreco(request.getPreco());
        plano.setDuracaoDias(request.getDuracaoDias());
        plano.setMaxDispositivos(request.getMaxDispositivos());

        return PlanoResponse.from(planoRepository.save(plano));
    }

    @Transactional(readOnly = true)
    public PlanoResponse buscarPorId(UUID id) {
        return PlanoResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<PlanoResponse> listarAtivos() {
        return planoRepository.findAllByAtivoTrue()
                .stream()
                .map(PlanoResponse::from)
                .toList();
    }

    @Transactional
    public void desativar(UUID id) {
        Plano plano = buscarEntidade(id);
        plano.desativar();
        planoRepository.save(plano);
        log.info("Plano {} desativado", id);
    }

    @Transactional(readOnly = true)
    public Plano buscarEntidade(UUID id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano", id));
    }
}
