package com.br.reservas.service;

import com.br.reservas.dto.SalaRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Sala;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;

    public SalaService(SalaRepository salaRepository, ReservaRepository reservaRepository) {
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<Sala> listarSalas() {
        return salaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sala buscarPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));
    }

    @Transactional
    public Sala criar(SalaRequestDTO dto) {
        return salaRepository.save(new Sala(dto.getNome(), dto.getCapacidade()));
    }

    @Transactional
    public Sala atualizar(Long id, SalaRequestDTO dto) {
        Sala sala = buscarPorId(id);
        sala.alterarDados(dto.getNome(), dto.getCapacidade());
        return salaRepository.save(sala);
    }

    @Transactional
    public Sala ativar(Long id) {
        Sala sala = buscarPorId(id);
        sala.ativar();
        return salaRepository.save(sala);
    }

    @Transactional
    public Sala desativar(Long id) {
        Sala sala = buscarPorId(id);
        sala.desativar();
        return salaRepository.save(sala);
    }

    /**
     * Exclui a sala só quando ela nunca foi reservada.
     * Se houver histórico, a sala deve ser desativada: apagá-la removeria as reservas associadas.
     */
    @Transactional
    public void excluir(Long id) {
        Sala sala = buscarPorId(id);
        if (reservaRepository.existsBySala_Id(id)) {
            throw new ConflitoException("Sala possui reservas e não pode ser excluída. Desative-a em vez disso.");
        }
        salaRepository.delete(sala);
    }
}
