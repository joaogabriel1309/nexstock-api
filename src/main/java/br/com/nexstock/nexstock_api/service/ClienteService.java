package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.domain.entity.Cliente;
import br.com.nexstock.nexstock_api.dto.request.ClienteRequest;
import br.com.nexstock.nexstock_api.dto.response.ClienteResponse;
import br.com.nexstock.nexstock_api.exception.RecursoNaoEncontradoException;
import br.com.nexstock.nexstock_api.exception.RegraDeNegocioException;
import br.com.nexstock.nexstock_api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        log.info("Cadastrando cliente: {}", request.getEmail());

        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new RegraDeNegocioException(
                "Email '" + request.getEmail() + "' já está em uso."
            );
        }

        Cliente cliente = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .documento(request.getDocumento())
                .telefone(request.getTelefone())
                .build();

        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(UUID id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);

        boolean emailAlterado = !cliente.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailAlterado && clienteRepository.existsByEmail(request.getEmail())) {
            throw new RegraDeNegocioException(
                "Email '" + request.getEmail() + "' já está em uso."
            );
        }

        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setDocumento(request.getDocumento());
        cliente.setTelefone(request.getTelefone());

        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        return ClienteResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponse::from)
                .toList();
    }

    @Transactional
    public void desativar(UUID id) {
        Cliente cliente = buscarEntidade(id);
        cliente.desativar();
        clienteRepository.save(cliente);
        log.info("Cliente {} desativado", id);
    }

    @Transactional
    public void reativar(UUID id) {
        Cliente cliente = buscarEntidade(id);
        cliente.reativar();
        clienteRepository.save(cliente);
        log.info("Cliente {} reativado", id);
    }


    @Transactional(readOnly = true)
    public Cliente buscarEntidade(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
