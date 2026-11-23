package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.regex.Pattern;

/**
 * Pessoa que faz reservas.
 *
 * <p>O e-mail é o identificador de negócio: é normalizado (sem espaços, em minúsculas)
 * e não pode se repetir. A unicidade também é garantida pelo banco, como rede de segurança.</p>
 */
@Entity
public class Usuario {

    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int TAMANHO_MAXIMO_EMAIL = 150;
    /** Validação simples de formato: algo@algo.algo, sem espaços. Não substitui confirmação por e-mail. */
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Gerado pelo banco. Fica nulo até o usuário ser salvo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
    private String nome;

    @Column(nullable = false, unique = true, length = TAMANHO_MAXIMO_EMAIL)
    private String email;

    protected Usuario() {
        // Construtor exigido pelo JPA.
    }

    public Usuario(String nome, String email) {
        this.nome = validarNome(nome);
        this.email = normalizarEmail(email);
    }

    public void alterarDados(String nome, String email) {
        // Valida tudo antes de atribuir: se algo falhar, o usuário não fica com dados pela metade.
        String nomeValidado = validarNome(nome);
        String emailValidado = normalizarEmail(email);
        this.nome = nomeValidado;
        this.email = emailValidado;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Valida e normaliza um e-mail. Público para que o serviço consiga comparar
     * o e-mail novo com o cadastrado antes de alterar o usuário.
     */
    public static String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DadosInvalidosException("E-mail do usuário é obrigatório.");
        }
        String emailLimpo = email.trim().toLowerCase();
        if (emailLimpo.length() > TAMANHO_MAXIMO_EMAIL || !PADRAO_EMAIL.matcher(emailLimpo).matches()) {
            throw new DadosInvalidosException("E-mail inválido.");
        }
        return emailLimpo;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Nome do usuário é obrigatório.");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new DadosInvalidosException("Nome do usuário pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        return nomeLimpo;
    }
}
