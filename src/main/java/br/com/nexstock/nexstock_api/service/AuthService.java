package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.dto.request.LoginRequest;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.LoginResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ContratoService contratoService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailAndContratoId(request.getEmail(), request.getContratoId())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

        if (!usuario.isEnabled()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario);
        log.info("Login realizado — usuário: {} | contrato: {}", usuario.getEmail(), usuario.getContrato().getId());

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .contratoId(usuario.getContrato().getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .expiracaoEmMs(jwtService.getExpiracaoMs())
                .build();
    }

    @Transactional
    public LoginResponse registrar(RegistroUsuarioRequest request) {
        Contrato contrato = contratoService.buscarEntidadeVigente(request.getContratoId());

        if (usuarioRepository.existsByEmailAndContratoId(request.getEmail(), contrato.getId())) {
            throw new RegraDeNegocioException(
                    "Email '" + request.getEmail() + "' já está em uso neste contrato."
            );
        }

        Usuario usuario = Usuario.builder()
                .contrato(contrato)
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole() != null ? request.getRole() : Role.OPERADOR)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuário registrado: {} | contrato: {}", usuario.getEmail(), contrato.getId());

        String token = jwtService.gerarToken(usuario);

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(usuario.getId())
                .contratoId(contrato.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .expiracaoEmMs(jwtService.getExpiracaoMs())
                .build();
    }
}
