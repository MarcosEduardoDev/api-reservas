package com.br.reservas.dto;

import com.br.reservas.model.Usuario;

/** Representação de um usuário na API. */
public record UsuarioResponseDTO(Long id, String nome, String email) {

    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
