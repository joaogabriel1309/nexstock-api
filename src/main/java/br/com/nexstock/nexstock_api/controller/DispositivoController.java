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

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<DispositivoResponse>> listarPorContrato(@PathVariable UUID contratoId) {
        return ResponseEntity.ok(dispositivoService.listarPorContrato(contratoId));
    }

    @GetMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<DispositivoResponse> buscarPorId(
            @PathVariable UUID contratoId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(dispositivoService.buscarPorId(contratoId, id));
    }
}
