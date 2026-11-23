package com.br.reservas.controller;

import com.br.reservas.dto.UsuarioRequestDTO;
import com.br.reservas.model.Usuario;
import com.br.reservas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Usuario criarUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        return usuarioService.salvar(usuarioDTO);
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public Usuario listarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Usuario alterarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        return usuarioService.atualizar(id, usuarioDTO);
    }

    @DeleteMapping("/{id}")
    public void deletarUsuario(@PathVariable Long id) {
         usuarioService.deletar(id);
    }


}
