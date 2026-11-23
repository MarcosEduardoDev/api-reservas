package com.br.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados recebidos para criar ou alterar um usuário. */
public class UsuarioRequestDTO {

    @NotBlank(message = "Nome do usuário é obrigatório.")
    @Size(max = 100, message = "Nome do usuário pode ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "E-mail do usuário é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "E-mail pode ter no máximo 150 caracteres.")
    private String email;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
