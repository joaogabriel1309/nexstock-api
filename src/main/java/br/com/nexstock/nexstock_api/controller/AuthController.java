package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.LoginRequest;
import br.com.nexstock.nexstock_api.dto.request.RegistroUsuarioRequest;
import br.com.nexstock.nexstock_api.dto.response.LoginResponse;
import br.com.nexstock.nexstock_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registrar")
    public ResponseEntity<LoginResponse> registrar(@RequestBody @Valid RegistroUsuarioRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }
}