package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.ProdutoRequest;
import br.com.nexstock.nexstock_api.dto.response.ProdutoResponse;
import br.com.nexstock.nexstock_api.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
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

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<ProdutoResponse>> listar(
            @PathVariable UUID contratoId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime atualizadoDepois) {

        if (atualizadoDepois != null) {
            return ResponseEntity.ok(produtoService.listarParaSync(contratoId, atualizadoDepois));
        }

        return ResponseEntity.ok(produtoService.listarAtivos(contratoId));
    }

    @GetMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(
            @PathVariable UUID contratoId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarPorId(contratoId, id));
    }

    @PutMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable UUID contratoId,
            @PathVariable UUID id,
            @RequestBody @Valid ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(contratoId, id, request));
    }

    @DeleteMapping("/contrato/{contratoId}/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID contratoId,
            @PathVariable UUID id,
            @RequestParam UUID dispositivoId) {
        produtoService.deletar(contratoId, id, dispositivoId);
        return ResponseEntity.noContent().build();
    }
}
