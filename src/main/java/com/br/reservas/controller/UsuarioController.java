package com.br.reservas.controller;

import com.br.reservas.dto.UsuarioRequestDTO;
import com.br.reservas.dto.UsuarioResponseDTO;
import com.br.reservas.model.Usuario;
import com.br.reservas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario usuario = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.from(usuario));
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.listarTodos().stream()
                .map(UsuarioResponseDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return UsuarioResponseDTO.from(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto) {
        return UsuarioResponseDTO.from(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
    }
}
