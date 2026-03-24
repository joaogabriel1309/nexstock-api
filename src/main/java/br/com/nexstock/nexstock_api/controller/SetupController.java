package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.repository.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final ClienteRepository clienteRepository;
    private final PlanoRepository planoRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<SetupResponse> setup(@RequestBody SetupRequest request) {

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nome(request.empresaNome())
                .email(request.empresaEmail())
                .documento(request.empresaDocumento())
                .telefone(request.empresaTelefone())
                .build());

        Plano plano = planoRepository.save(Plano.builder()
                .nome(request.planoNome())
                .preco(BigDecimal.valueOf(request.planoPreco()))
                .duracaoDias(request.planoDias())
                .maxDispositivos(request.planoMaxDispositivos())
                .ativo(true)
                .build());

        Contrato contrato = contratoRepository.save(Contrato.builder()
                .plano(plano)
                .dataInicio(LocalDate.now())
                .dataFim(LocalDate.now().plusDays(plano.getDuracaoDias()))
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome(request.usuarioNome())
                .email(request.usuarioEmail())
                .senha(passwordEncoder.encode(request.usuarioSenha()))
                .role(Role.ADMIN)
                .build());

        return ResponseEntity.ok(new SetupResponse(contrato.getId()));
    }

    public record SetupRequest(
            String empresaNome, String empresaEmail,
            String empresaDocumento, String empresaTelefone,
            String planoNome, double planoPreco,
            int planoDias, int planoMaxDispositivos,
            String usuarioNome, String usuarioEmail, String usuarioSenha
    ) {}

    public record SetupResponse(UUID contratoId) {}
}