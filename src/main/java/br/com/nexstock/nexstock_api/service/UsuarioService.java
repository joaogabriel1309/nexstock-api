package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.dto.request.UsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.UsuarioResponse;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodoPorEmpresa(UUID empresaId) {
        log.info("Listando usuários da empresa: {}", empresaId);
        return usuarioRepository.findAllByEmpresaIdAndDeletadoEmIsNull(empresaId)
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UsuarioRequest dto) {
        log.info("Buscando usuário {} da empresa {}", dto.getId(), dto.getEmpresaId());
        return usuarioRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(dto.getId(), dto.getEmpresaId())
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou não pertence a esta empresa."));
    }

    @Transactional
    public void deletarUsuario(UUID usuarioId, UUID empresaId) {
        log.info("Deletando usuário {} da empresa {}", usuarioId, empresaId);

        var usuario = usuarioRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para exclusão."));

        usuario.setDeletadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
}