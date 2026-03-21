package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.EmpresaRequest;
import br.com.nexstock.nexstock_api.dto.response.EmpresaResponse;
import br.com.nexstock.nexstock_api.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponse> criar(@RequestBody @Valid EmpresaRequest request) {
        EmpresaResponse response = empresaService.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<EmpresaResponse>> listar(@PathVariable UUID contratoId) {
        return ResponseEntity.ok(empresaService.listarPorContrato(contratoId));
    }

    @GetMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<EmpresaResponse> buscarPorId(
            @PathVariable UUID contratoId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(empresaService.buscarPorId(contratoId, id));
    }

    @PutMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<EmpresaResponse> atualizar(
            @PathVariable UUID contratoId,
            @PathVariable UUID id,
            @RequestBody @Valid EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.atualizar(contratoId, id, request));
    }

    @PatchMapping("/contrato/{contratoId}/{id}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID contratoId,
            @PathVariable UUID id) {
        empresaService.desativar(contratoId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/contrato/{contratoId}/{id}/reativar")
    public ResponseEntity<Void> reativar(
            @PathVariable UUID contratoId,
            @PathVariable UUID id) {
        empresaService.reativar(contratoId, id);
        return ResponseEntity.noContent().build();
    }
}