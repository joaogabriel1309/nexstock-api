package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Dispositivo;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.dto.request.DispositivoRequest;
import br.com.nexstock.nexstock_api.dto.response.DispositivoResponse;
import br.com.nexstock.nexstock_api.exception.LimiteDispositivosException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.DispositivoRepository;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
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
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public DispositivoResponse registrar(DispositivoRequest request) {
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", request.getEmpresaId()));

        var contratoAtivo = empresa.getContrato();
        if (contratoAtivo == null || !contratoAtivo.estaVigente()) {
            throw new br.com.nexstock.nexstock_api.exception.ContratoInativoException("Empresa sem contrato ativo.");
        }

        long totalAtual = dispositivoRepository.countByEmpresaId(empresa.getId());
        int limite = contratoAtivo.getPlano().getMaxDispositivos();

        if (totalAtual >= limite) {
            throw new LimiteDispositivosException(limite, contratoAtivo.getPlano().getNome());
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", request.getUsuarioId()));

        Dispositivo dispositivo = Dispositivo.builder()
                .nome(request.getNome())
                .sistema(request.getSistema())
                .empresa(empresa)
                .usuario(usuario)
                .build();

        Dispositivo salvo = dispositivoRepository.save(dispositivo);

        log.info("Dispositivo {} ('{}') registrado para a empresa {}", salvo.getId(), salvo.getNome(), empresa.getNome());

        return DispositivoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public List<DispositivoResponse> listarPorUsuario(UUID empresaId, UUID usuarioId) {
        log.info("Listando dispositivos do usuário {} da empresa {}", usuarioId, empresaId);

        return dispositivoRepository.findAllByEmpresaIdAndUsuarioIdAndDeletadoEmIsNull(empresaId, usuarioId)
                .stream()
                .map(DispositivoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DispositivoResponse> listarPorEmpresa(UUID empresaId) {
        return dispositivoRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(DispositivoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Dispositivo buscarEntidade(UUID empresaId, UUID id) {
        return dispositivoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Dispositivo", id));
    }

    @Transactional
    public void remover(UUID empresaId, UUID id) {
        Dispositivo dispositivo = buscarEntidade(empresaId, id);
        dispositivoRepository.delete(dispositivo);
        log.info("Dispositivo {} removido da empresa {}", id, empresaId);
    }
}