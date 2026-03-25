package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.domain.entity.Usuario;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.dto.request.LoginRequest;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.LoginResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock
    EmpresaRepository       empresaRepository;
    @Mock JwtService        jwtService;
    @Mock PasswordEncoder   passwordEncoder;

    @InjectMocks AuthService authService;

    private UUID empresaId;
    private Empresa empresa;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        empresa   = Empresa.builder().id(empresaId).nome("NexStock Store").ativo(true).build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .empresa(empresa)
                .email("joao@nexstock.com")
                .senha("$2a$hash")
                .role(Role.ADMIN)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("deve retornar token quando credenciais corretas")
        void deveRetornarTokenComCredenciaisCorretas() {
            var request = new LoginRequest("joao@nexstock.com", "senha123");

            when(usuarioRepository.findByEmail("joao@nexstock.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha123", "$2a$hash")).thenReturn(true);
            when(jwtService.gerarToken(usuario)).thenReturn("token.jwt.aqui");
            when(jwtService.getExpiracaoMs()).thenReturn(86400000L);

            LoginResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("token.jwt.aqui");
            verify(usuarioRepository).findByEmail("joao@nexstock.com");
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando senha incorreta")
        void deveLancarExcecaoQuandoSenhaIncorreta() {
            var request = new LoginRequest("joao@nexstock.com", "senha_errada");

            when(usuarioRepository.findByEmail("joao@nexstock.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha_errada", "$2a$hash")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Email ou senha inválidos");
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando usuário inativo")
        void deveLancarExcecaoQuandoUsuarioInativo() {
            usuario.setAtivo(false);
            var request = new LoginRequest("joao@nexstock.com", "senha123");
            when(usuarioRepository.findByEmail("joao@nexstock.com")).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando a empresa está inativa")
        void deveLancarExcecaoQuandoEmpresaInativa() {
            empresa.setAtivo(false);
            var request = new LoginRequest("joao@nexstock.com", "senha123");
            when(usuarioRepository.findByEmail("joao@nexstock.com")).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Usuário ou Empresa inativos");
        }
    }

    @Nested
    @DisplayName("registrar")
    class Registrar {
        private void mockUsuarioLogado(String email) {
            Authentication auth = mock(Authentication.class);
            when(auth.getName()).thenReturn(email);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);

            SecurityContextHolder.setContext(securityContext);
        }

        @Test
        @DisplayName("deve registrar novo usuário com sucesso")
        void deveRegistrarComSucesso() {
            mockUsuarioLogado("admin@nexstock.com");

            when(usuarioRepository.findByEmail("admin@nexstock.com")).thenReturn(Optional.of(usuario));

            var request = RegistroUsuarioRequest.builder()
                    .empresaId(empresaId)
                    .nome("Novo User")
                    .email("novo@nexstock.com")
                    .senha("senha123")
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(usuarioRepository.existsByEmail("novo@nexstock.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
            when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtService.gerarToken(any())).thenReturn("token.jwt");

            LoginResponse response = authService.registrar(request);

            assertThat(response.getEmail()).isEqualTo("novo@nexstock.com");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando email já existe")
        void deveLancarExcecaoQuandoEmailDuplicado() {
            mockUsuarioLogado("admin@nexstock.com");
            when(usuarioRepository.findByEmail("admin@nexstock.com")).thenReturn(Optional.of(usuario));

            var request = RegistroUsuarioRequest.builder()
                    .empresaId(empresaId)
                    .email("duplicado@nexstock.com")
                    .build();

            when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
            when(usuarioRepository.existsByEmail("duplicado@nexstock.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(request))
                    .isInstanceOf(RegraDeNegocioException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando ADMIN tenta registrar usuário em outra empresa")
        void deveLancarExcecaoQuandoAdminTentaRegistrarEmEmpresaAlheia() {
            mockUsuarioLogado("admin@empresaA.com");
            when(usuarioRepository.findByEmail("admin@empresaA.com")).thenReturn(Optional.of(usuario));

            UUID empresaBId = UUID.randomUUID();
            var request = RegistroUsuarioRequest.builder()
                    .empresaId(empresaBId)
                    .email("novo@empresaB.com")
                    .build();

            assertThatThrownBy(() -> authService.registrar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Você não tem permissão para registrar usuários em outra empresa.");
        }

        @Test
        @DisplayName("deve lançar exceção quando a empresa informada não existe")
        void deveLancarExcecaoQuandoEmpresaNaoExiste() {
            mockUsuarioLogado("admin@nexstock.com");
            usuario.setEmpresa(empresa);
            when(usuarioRepository.findByEmail("admin@nexstock.com")).thenReturn(Optional.of(usuario));

            UUID idEmpresaQueNaoSeraAchada = empresa.getId();

            var request = RegistroUsuarioRequest.builder()
                    .empresaId(idEmpresaQueNaoSeraAchada)
                    .email("teste@teste.com")
                    .build();

            when(empresaRepository.findById(idEmpresaQueNaoSeraAchada)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.registrar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Empresa não encontrada");
        }

        @Test
        @DisplayName("Deve lançar exceção quando o e-mail já está cadastrado")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            mockUsuarioLogado("admin@nexstock.com");
            usuario.setEmpresa(empresa);
            when(usuarioRepository.findByEmail("admin@nexstock.com")).thenReturn(Optional.of(usuario));

            var request = RegistroUsuarioRequest.builder()
                    .empresaId(empresa.getId())
                    .email("duplicado@nexstock.com")
                    .build();

            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(usuarioRepository.existsByEmail("duplicado@nexstock.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("O e-mail 'duplicado@nexstock.com' já está em uso.");
        }

        @Test
        @DisplayName("Deve registrar um usuário com sucesso")
        void deveRegistrarUsuarioComSucesso() {
            mockUsuarioLogado("admin@nexstock.com");
            usuario.setEmpresa(empresa);
            when(usuarioRepository.findByEmail("admin@nexstock.com")).thenReturn(Optional.of(usuario));
            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(usuarioRepository.existsByEmail("novo@nexstock.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("senhaCripto");

            var request = RegistroUsuarioRequest.builder()
                    .empresaId(empresa.getId())
                    .email("novo@nexstock.com")
                    .nome("Novo Usuário")
                    .senha("123456")
                    .build();

            authService.registrar(request);

            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }
}