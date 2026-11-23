package com.br.reservas.service;

import com.br.reservas.dto.UsuarioRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário inexistente."));
    }

    @Transactional
    public Usuario criar(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario(dto.getNome(), dto.getEmail());
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarPorId(id);
        String emailNormalizado = Usuario.normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, id)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
        usuario.alterarDados(dto.getNome(), dto.getEmail());
        return usuarioRepository.save(usuario);
    }

    /** Exclui o usuário só se ele nunca fez reservas, para não apagar o histórico. */
    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        if (reservaRepository.existsByUsuario_Id(id)) {
            throw new ConflitoException("Usuário possui reservas e não pode ser excluído.");
        }
        usuarioRepository.delete(usuario);
    }
}
