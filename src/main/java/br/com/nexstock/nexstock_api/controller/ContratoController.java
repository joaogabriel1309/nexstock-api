package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.ContratoRequest;
import br.com.nexstock.nexstock_api.dto.response.ContratoResponse;
import br.com.nexstock.nexstock_api.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    public ResponseEntity<ContratoResponse> contratar(@RequestBody @Valid ContratoRequest request) {
        ContratoResponse response = contratoService.contratar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContratoResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(contratoService.listarPorCliente(clienteId));
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<ContratoResponse> renovar(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.renovar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        contratoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<Void> suspender(@PathVariable UUID id) {
        contratoService.suspender(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        contratoService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
