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
        Contrato contrato = contratoService.buscarEntidadeVigente(request.getContratoId());

        if (empresaRepository.existsByCpfCnpjAndContratoId(request.getCpfCnpj(), contrato.getId())) {
            throw new RegraDeNegocioException(
                    "CPF/CNPJ '" + request.getCpfCnpj() + "' já cadastrado neste contrato."
            );
        }

        Empresa empresa = Empresa.builder()
                .contrato(contrato)
                .nome(request.getNome())
                .razaoSocial(request.getRazaoSocial())
                .cpfCnpj(request.getCpfCnpj())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .build();

        Empresa salva = empresaRepository.save(empresa);
        log.info("Empresa {} criada para contrato {}", salva.getId(), contrato.getId());
        return EmpresaResponse.from(salva);
    }

    @Transactional
    public EmpresaResponse atualizar(UUID contratoId, UUID id, EmpresaRequest request) {
        Empresa empresa = buscarEntidade(contratoId, id);

        boolean cpfCnpjAlterado = !empresa.getCpfCnpj().equals(request.getCpfCnpj());
        if (cpfCnpjAlterado && empresaRepository.existsByCpfCnpjAndContratoId(request.getCpfCnpj(), contratoId)) {
            throw new RegraDeNegocioException(
                    "CPF/CNPJ '" + request.getCpfCnpj() + "' já cadastrado neste contrato."
            );
        }

        empresa.setNome(request.getNome());
        empresa.setRazaoSocial(request.getRazaoSocial());
        empresa.setCpfCnpj(request.getCpfCnpj());
        empresa.setEmail(request.getEmail());
        empresa.setTelefone(request.getTelefone());

        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(UUID contratoId, UUID id) {
        return EmpresaResponse.from(buscarEntidade(contratoId, id));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarPorContrato(UUID contratoId) {
        return empresaRepository.findAllByContratoId(contratoId)
                .stream()
                .map(EmpresaResponse::from)
                .toList();
    }

    @Transactional
    public void desativar(UUID contratoId, UUID id) {
        Empresa empresa = buscarEntidade(contratoId, id);
        empresa.desativar();
        empresaRepository.save(empresa);
        log.info("Empresa {} desativada", id);
    }

    @Transactional
    public void reativar(UUID contratoId, UUID id) {
        Empresa empresa = buscarEntidade(contratoId, id);
        empresa.reativar();
        empresaRepository.save(empresa);
        log.info("Empresa {} reativada", id);
    }

    @Transactional(readOnly = true)
    public Empresa buscarEntidade(UUID contratoId, UUID id) {
        return empresaRepository.findByIdAndContratoId(id, contratoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
    }
}