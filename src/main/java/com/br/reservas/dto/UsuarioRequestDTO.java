package com.br.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para criar ou alterar um usuário.
 *
 * <p>O formato do e-mail não é validado aqui: a validação é feita no domínio, depois do trim
 * e da normalização. Assim "  ana@exemplo.com  " é aceito, como o README descreve.</p>
 */
public class UsuarioRequestDTO {

    @NotBlank(message = "Nome do usuário é obrigatório.")
    @Size(max = 100, message = "Nome do usuário pode ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "E-mail do usuário é obrigatório.")
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
