package br.com.nexstock.nexstock_api.controller;

import br.com.nexstock.nexstock_api.dto.request.SetupRequest;
import br.com.nexstock.nexstock_api.service.SetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void realizarSetup(@RequestBody @Valid SetupRequest dto){
        setupService.realizarSetup(dto);
    }
}
