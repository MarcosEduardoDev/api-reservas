package com.br.reservas.repository;

import com.br.reservas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    /** Verifica se outro usuário (diferente de {@code id}) já usa este e-mail. */
    boolean existsByEmailAndIdNot(String email, Long id);
}
