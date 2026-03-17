package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.PlanoRequest;
import br.com.nexstock.nexstock_api.dto.response.PlanoResponse;
import br.com.nexstock.nexstock_api.service.PlanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @PostMapping
    public ResponseEntity<PlanoResponse> criar(@RequestBody @Valid PlanoRequest request) {
        PlanoResponse response = planoService.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(planoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PlanoResponse>> listarAtivos() {
        return ResponseEntity.ok(planoService.listarAtivos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid PlanoRequest request) {
        return ResponseEntity.ok(planoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        planoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
