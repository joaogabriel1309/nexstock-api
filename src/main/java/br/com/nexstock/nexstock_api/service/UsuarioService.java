package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.UsuarioResponse;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public UsuarioResponse criar(RegistroUsuarioRequest dto) {
        usuarioRepository.findByEmailAndDeletadoEmIsNull(dto.getEmail())
                .ifPresent(u -> {
                    throw new RegraDeNegocioException("Este e-mail já está cadastrado no sistema.");
                });

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RegraDeNegocioException("Empresa não encontrada para o ID fornecido."));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.getNome());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setRole(dto.getRole());
        novoUsuario.setEmpresa(empresa);
        novoUsuario.setAtivo(true);
        novoUsuario.setCriadoEm(LocalDateTime.now());

        String senhaCriptografada = passwordEncoder.encode(dto.getSenha());
        novoUsuario.setSenha(senhaCriptografada);

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        log.info("Iniciando criação de novo usuário: {} para empresa: {}", dto.getEmail(), dto.getEmpresaId());

        return UsuarioResponse.from(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodoPorEmpresa(UUID empresaId) {
        log.info("Listando usuários da empresa: {}", empresaId);
        return usuarioRepository.findAllByEmpresaIdAndDeletadoEmIsNull(empresaId)
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID empresaId, UUID usuarioId) {
        log.info("Buscando usuário {} da empresa {}", usuarioId, empresaId);
        return usuarioRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(usuarioId, empresaId)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou não pertence a esta empresa."));
    }

    @Transactional
    public void deletarUsuario(UUID empresaId, UUID usuarioId) {
        log.info("Deletando usuário {} da empresa {}", usuarioId, empresaId);

        var usuario = usuarioRepository.findByIdAndEmpresaIdAndDeletadoEmIsNull(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para exclusão."));

        usuario.setDeletadoEm(LocalDateTime.now());
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}