package br.com.nexstock.nexstock_api.service;

import br.com.nexstock.nexstock_api.dto.response.PlanoResponse;
import br.com.nexstock.nexstock_api.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository planoRepository;

    @Transactional(readOnly = true)
    public List<PlanoResponse> listarAtivos() {
        return planoRepository.findByAtivoTrueOrderByPrecoAsc()
                .stream()
                .map(PlanoResponse::from)
                .toList();
    }
}
