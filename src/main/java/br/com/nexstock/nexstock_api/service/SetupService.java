package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.*;
import br.com.nexstock.nexstock_api.domain.enums.Role;
import br.com.nexstock.nexstock_api.dto.request.EmpresaRequest;
import br.com.nexstock.nexstock_api.dto.request.SetupRequest;
import br.com.nexstock.nexstock_api.dto.response.EmpresaResponse;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import br.com.nexstock.nexstock_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final EmpresaService empresaService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public void realizarSetup(SetupRequest dto) {

        EmpresaRequest empresa = EmpresaRequest.builder()
                .planoId(dto.getPlanoId())
                .nome(dto.getEmpresaNome())
                .razaoSocial(dto.getEmpresaNome())
                .cpfCnpj(dto.getEmpresaCpfCnpj())
                .build();
        EmpresaResponse empresaSalva = empresaService.criar(empresa);

        Empresa empresaEntity = empresaRepository.getReferenceById(empresaSalva.getId());

        Usuario admin = Usuario.builder()
                .nome(dto.getAdminNome())
                .email(dto.getAdminEmail())
                .senha(passwordEncoder.encode(dto.getAdminSenha()))
                .role(Role.ADMIN)
                .empresa(empresaEntity)
                .ativo(true)
                .build();

        usuarioRepository.save(admin);
    }
}