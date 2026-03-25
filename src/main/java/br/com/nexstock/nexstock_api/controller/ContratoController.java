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
@RequestMapping("/api/admin/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(ContratoResponse.from(contratoService.buscarEntidade(id)));
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<ContratoResponse> renovar(@PathVariable UUID id) {
        return ResponseEntity.ok(ContratoResponse.from(contratoService.renovarInternamente(id)));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        contratoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}