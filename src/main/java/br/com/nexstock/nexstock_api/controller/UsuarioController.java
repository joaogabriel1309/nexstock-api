package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.request.UsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.UsuarioResponse;
import br.com.nexstock.nexstock_api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> cria(@RequestBody @Valid RegistroUsuarioRequest request) {
        UsuarioResponse response = usuarioService.criar(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodoPorEmpresa(@RequestParam UUID empresaId) {
        return ResponseEntity.ok(usuarioService.listarTodoPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@RequestParam UUID empresaId, @PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @RequestParam UUID empresaId,
            @PathVariable UUID id) {

        usuarioService.deletarUsuario(empresaId, id);

        return ResponseEntity.noContent().build();
    }
}