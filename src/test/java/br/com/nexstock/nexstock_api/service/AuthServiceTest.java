package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.dto.request.LoginRequest;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.LoginResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ContratoService   contratoService;
    @Mock JwtService        jwtService;
    @Mock PasswordEncoder   passwordEncoder;

    @InjectMocks AuthService authService;

    private UUID contratoId;
    private Contrato contrato;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        contratoId = UUID.randomUUID();
        contrato   = Contrato.builder().id(contratoId).build();
        usuario    = Usuario.builder()
                .id(UUID.randomUUID())
                .contrato(contrato)
                .email("joao@nexstock.com")
                .senha("$2a$hash")
                .role(Role.OPERADOR)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("deve retornar token quando credenciais corretas")
        void deveRetornarTokenComCredenciaisCorretas() {
            var request = new LoginRequest(contratoId, "joao@nexstock.com", "senha123");

            when(usuarioRepository.findByEmailAndContratoId("joao@nexstock.com", contratoId))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha123", "$2a$hash")).thenReturn(true);
            when(jwtService.gerarToken(usuario)).thenReturn("token.jwt.aqui");
            when(jwtService.getExpiracaoMs()).thenReturn(86400000L);

            LoginResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("token.jwt.aqui");
            assertThat(response.getTipo()).isEqualTo("Bearer");
            assertThat(response.getEmail()).isEqualTo("joao@nexstock.com");
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando usuário não existe")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            var request = new LoginRequest(contratoId, "naoexiste@nexstock.com", "senha123");

            when(usuarioRepository.findByEmailAndContratoId(any(), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando senha errada")
        void deveLancarExcecaoQuandoSenhaErrada() {
            var request = new LoginRequest(contratoId, "joao@nexstock.com", "senhaerrada");

            when(usuarioRepository.findByEmailAndContratoId("joao@nexstock.com", contratoId))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senhaerrada", "$2a$hash")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando usuário inativo")
        void deveLancarExcecaoQuandoUsuarioInativo() {
            usuario.setAtivo(false);
            var request = new LoginRequest(contratoId, "joao@nexstock.com", "senha123");

            when(usuarioRepository.findByEmailAndContratoId("joao@nexstock.com", contratoId))
                    .thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("deve registrar usuário com role padrão OPERADOR")
        void deveRegistrarComRolePadraoOperador() {
            var request = RegistroUsuarioRequest.builder()
                    .contratoId(contratoId)
                    .nome("João")
                    .email("joao@nexstock.com")
                    .senha("senha123")
                    .build(); // role null → deve usar OPERADOR

            when(contratoService.buscarEntidadeVigente(contratoId)).thenReturn(contrato);
            when(usuarioRepository.existsByEmailAndContratoId(any(), any())).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
            when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtService.gerarToken(any())).thenReturn("token.jwt");
            when(jwtService.getExpiracaoMs()).thenReturn(86400000L);

            LoginResponse response = authService.registrar(request);

            assertThat(response.getRole()).isEqualTo("OPERADOR");
            verify(usuarioRepository).save(argThat(u -> u.getRole() == Role.OPERADOR));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando email duplicado no contrato")
        void deveLancarExcecaoQuandoEmailDuplicado() {
            var request = RegistroUsuarioRequest.builder()
                    .contratoId(contratoId)
                    .nome("João")
                    .email("joao@nexstock.com")
                    .senha("senha123")
                    .build();

            when(contratoService.buscarEntidadeVigente(contratoId)).thenReturn(contrato);
            when(usuarioRepository.existsByEmailAndContratoId("joao@nexstock.com", contratoId))
                    .thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já está em uso");
        }
    }
}