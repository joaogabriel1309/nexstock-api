package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Contrato;
import br.com.nexstock.nexstock_api.domain.entity.Empresa;
import br.com.nexstock.nexstock_api.dto.request.EmpresaRequest;
import br.com.nexstock.nexstock_api.dto.response.EmpresaResponse;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ContratoService   contratoService;

    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        if (empresaRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new RegraDeNegocioException(
                    "O CPF/CNPJ '" + request.getCpfCnpj() + "' já está cadastrado no NexStock."
            );
        }

        Contrato novoContrato = contratoService.gerarContratoInicial(request.getPlanoId());

        Empresa empresa = Empresa.builder()
                .contrato(novoContrato)
                .nome(request.getNome())
                .razaoSocial(request.getRazaoSocial())
                .cpfCnpj(request.getCpfCnpj())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .ativo(true)
                .build();

        Empresa salva = empresaRepository.save(empresa);

        log.info("Nova Empresa/Tenant criada: {} (ID: {}) com Contrato: {}",
                salva.getNome(), salva.getId(), novoContrato.getId());

        return EmpresaResponse.from(salva);
    }

    @Transactional
    public EmpresaResponse atualizar(UUID id, EmpresaRequest request) {
        Empresa empresa = buscarEntidade(id);

        if (!empresa.getCpfCnpj().equals(request.getCpfCnpj()) &&
                empresaRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new RegraDeNegocioException("Este CPF/CNPJ já pertence a outra empresa.");
        }

        empresa.setNome(request.getNome());
        empresa.setRazaoSocial(request.getRazaoSocial());
        empresa.setCpfCnpj(request.getCpfCnpj());
        empresa.setEmail(request.getEmail());
        empresa.setTelefone(request.getTelefone());

        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    @Transactional
    public void desativar(UUID id) {
        Empresa empresa = buscarEntidade(id);
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
        log.warn("Empresa {} foi DESATIVADA. Usuários não conseguirão logar.", id);
    }

    @Transactional(readOnly = true)
    public Empresa buscarEntidade(UUID id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
    }
}