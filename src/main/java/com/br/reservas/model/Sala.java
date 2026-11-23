package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Espaço físico que pode ser reservado, por exemplo "Sala 101".
 *
 * <p>Regras garantidas pela própria entidade:</p>
 * <ul>
 *   <li>o nome não pode ser vazio e tem no máximo 100 caracteres;</li>
 *   <li>a capacidade é a quantidade máxima de pessoas e precisa ser maior que zero;</li>
 *   <li>uma sala inativa (em manutenção, por exemplo) não recebe novas reservas.</li>
 * </ul>
 */
@Entity
public class Sala {

    private static final int TAMANHO_MAXIMO_NOME = 100;

    /** Gerado pelo banco. Fica nulo até a sala ser salva. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome exibido para as pessoas. Não é único de propósito: pode haver
     * salas com o mesmo nome em andares diferentes.
     */
    @Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
    private String nome;

    /** Quantidade máxima de pessoas. Integer (e não int) para que a ausência do valor possa ser detectada. */
    @Column(nullable = false)
    private Integer capacidade;

    /**
     * Indica se a sala aceita novas reservas. Desativar é preferível a excluir
     * quando a sala já tem histórico de reservas.
     */
    @Column(nullable = false)
    private boolean ativa;

    protected Sala() {
        // Construtor exigido pelo JPA.
    }

    public Sala(String nome, Integer capacidade) {
        this.nome = validarNome(nome);
        this.capacidade = validarCapacidade(capacidade);
        this.ativa = true;
    }

    public void alterarDados(String nome, Integer capacidade) {
        // Valida tudo antes de atribuir: se algo falhar, a sala não fica com dados pela metade.
        String nomeValidado = validarNome(nome);
        Integer capacidadeValidada = validarCapacidade(capacidade);
        this.nome = nomeValidado;
        this.capacidade = capacidadeValidada;
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public boolean isAtiva() {
        return ativa;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Nome da sala é obrigatório.");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new DadosInvalidosException("Nome da sala pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        return nomeLimpo;
    }

    private static Integer validarCapacidade(Integer capacidade) {
        if (capacidade == null || capacidade <= 0) {
            throw new DadosInvalidosException("A capacidade deve ser maior que zero.");
        }
        return capacidade;
    }
}
