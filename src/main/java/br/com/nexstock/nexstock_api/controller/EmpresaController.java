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

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(EmpresaResponse.from(empresaService.buscarEntidade(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        empresaService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}