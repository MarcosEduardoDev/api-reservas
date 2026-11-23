package com.br.reservas.controller;

import com.br.reservas.dto.SalaRequestDTO;
import com.br.reservas.dto.SalaResponseDTO;
import com.br.reservas.model.Sala;
import com.br.reservas.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List<SalaResponseDTO> listar() {
        return salaService.listarSalas().stream()
                .map(SalaResponseDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SalaResponseDTO buscarPorId(@PathVariable Long id) {
        return SalaResponseDTO.from(salaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> criar(@Valid @RequestBody SalaRequestDTO dto) {
        Sala sala = salaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(SalaResponseDTO.from(sala));
    }

    @PutMapping("/{id}")
    public SalaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody SalaRequestDTO dto) {
        return SalaResponseDTO.from(salaService.atualizar(id, dto));
    }

    /** Sala inativa não recebe novas reservas. As reservas já existentes são mantidas. */
    @PatchMapping("/{id}/ativar")
    public SalaResponseDTO ativar(@PathVariable Long id) {
        return SalaResponseDTO.from(salaService.ativar(id));
    }

    @PatchMapping("/{id}/desativar")
    public SalaResponseDTO desativar(@PathVariable Long id) {
        return SalaResponseDTO.from(salaService.desativar(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        salaService.excluir(id);
    }
}
