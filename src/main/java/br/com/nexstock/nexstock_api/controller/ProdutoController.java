package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid ProdutoRequest request) {
        ProdutoResponse response = produtoService.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarAtivos(@RequestParam UUID empresaId) {
        return ResponseEntity.ok(produtoService.listarAtivos(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(
            @RequestParam UUID empresaId,
            @PathVariable UUID id) {
        ProdutoResponse response = produtoService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(
            @RequestParam UUID empresaId,
            @PathVariable UUID id,
            @RequestBody @Valid ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(empresaId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @RequestParam UUID empresaId,
            @PathVariable UUID id) {
        produtoService.deletar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}