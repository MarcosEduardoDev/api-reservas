package com.br.reservas.controller;

import com.br.reservas.dto.SalaRequestDTO;
import com.br.reservas.model.Sala;
import com.br.reservas.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List<Sala> listarSalas(){
        return salaService.listarSalas();
    }

    @GetMapping("/{id}")
    public Sala listarPorId(@PathVariable Long id){
        return salaService.listarPorId(id);
    }

    @PostMapping
    public Sala salvarSala(@Valid @RequestBody SalaRequestDTO salaDTO){
        return salaService.salvarSala(salaDTO);
    }

    @PutMapping("/{id}")
    public Sala atualizarSala(@PathVariable Long id, @Valid @RequestBody SalaRequestDTO salaDTO){
        return salaService.atualizarSala(id, salaDTO);
    }

    @DeleteMapping("/{id}")
    public void deletarSala(@PathVariable Long id){
        salaService.deletarSala(id);
    }

}
