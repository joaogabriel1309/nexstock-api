package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.SyncRequest;
import br.com.nexstock.nexstock_api.dto.response.SyncResponse;
import br.com.nexstock.nexstock_api.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final SyncService syncService;


    @PostMapping
    public ResponseEntity<SyncResponse> sincronizar(@RequestBody @Valid SyncRequest request) {
        log.info("POST /api/sync — contrato: {} | dispositivo: {}",
                request.getContratoId(), request.getDispositivoId());

        SyncResponse response = syncService.processar(request);
        return ResponseEntity.ok(response);
    }
}
