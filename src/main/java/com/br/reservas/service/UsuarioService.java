package com.br.reservas.service;

import com.br.reservas.dto.UsuarioRequestDTO;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos(){
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id){

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário inexistente."));
    }

//  Cria e grava um usuário.
    @Transactional
    public Usuario salvar(UsuarioRequestDTO dto) {

        Usuario usuario = new Usuario(dto.getNome(), dto.getEmail());
        return usuarioRepository.save(usuario);
    }

//  Busca o usuário, altera os seus dados e grava a atualização.
    @Transactional
    public Usuario atualizar(Long id, UsuarioRequestDTO dto) {

        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioAtualizado = usuario.get();
            usuarioAtualizado.setNome(dto.getNome());
            usuarioAtualizado.setEmail(dto.getEmail());

            return usuarioRepository.save(usuarioAtualizado);
        } else {
            throw new ResourceNotFoundException("Usuário inexistente.");
        }
    }


//  Verifica se ele existe e depois o remove.
    @Transactional
    public void deletar(Long id) {

        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Usuário inexistente.");
        }
    }
}
