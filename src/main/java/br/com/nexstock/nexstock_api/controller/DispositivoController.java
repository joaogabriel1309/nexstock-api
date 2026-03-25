package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.DispositivoRequest;
import br.com.nexstock.nexstock_api.dto.response.DispositivoResponse;
import br.com.nexstock.nexstock_api.service.DispositivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {

    private final DispositivoService dispositivoService;

    @PostMapping
    public ResponseEntity<DispositivoResponse> registrar(@RequestBody @Valid DispositivoRequest request) {
        DispositivoResponse response = dispositivoService.registrar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<DispositivoResponse>> listarPorEmpresa(@PathVariable UUID empresaId) {
        return ResponseEntity.ok(dispositivoService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispositivoResponse> buscarPorId(
            @RequestParam UUID empresaId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(DispositivoResponse.from(dispositivoService.buscarEntidade(empresaId, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@RequestParam UUID empresaId, @PathVariable UUID id) {
        dispositivoService.remover(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}