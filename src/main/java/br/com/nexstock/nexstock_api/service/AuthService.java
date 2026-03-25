package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.dto.request.LoginRequest;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.LoginResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

        if (!usuario.isEnabled() || !usuario.getEmpresa().getAtivo()) {
            throw new BadCredentialsException("Acesso negado: Usuário ou Empresa inativos");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario);

        log.info("Login realizado — usuário: {} | Empresa: {}", usuario.getEmail(), usuario.getEmpresa().getNome());

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresa().getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .expiracaoEmMs(jwtService.getExpiracaoMs())
                .build();
    }

    @Transactional
    public LoginResponse registrar(RegistroUsuarioRequest request) {
        var empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RegraDeNegocioException("Empresa não encontrada"));

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RegraDeNegocioException("O e-mail '" + request.getEmail() + "' já está em uso.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole() != null ? request.getRole() : Role.OPERADOR)
                .empresa(empresa)
                .build();

        usuarioRepository.save(usuario);

        log.info("Novo usuário registrado: {} na empresa: {}", usuario.getEmail(), empresa.getNome());

        String token = jwtService.gerarToken(usuario);

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .empresaId(empresa.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .expiracaoEmMs(jwtService.getExpiracaoMs())
                .build();
    }
}