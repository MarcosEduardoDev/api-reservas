package com.br.reservas.service;

import com.br.reservas.dto.SalaRequestDTO;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Sala;
import com.br.reservas.repository.SalaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public List<Sala> listarSalas() {
        return salaRepository.findAll();
    }

    public Sala listarPorId(Long id){
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));

    }
//  Cria uma sala e salva no banco.
    @Transactional
    public Sala salvarSala(SalaRequestDTO dto){

        Sala sala = new Sala(dto.getNome(), dto.getCapacidade(), true);

        return salaRepository.save(sala);
    }

//  Busca a sala, altera seus dados e grava a atualização.
    @Transactional
    public Sala atualizarSala(Long id, SalaRequestDTO dto){

        Sala salaEncontrada = salaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));

            salaEncontrada.setNome(dto.getNome());
            salaEncontrada.setCapacidade(dto.getCapacidade());

            return salaRepository.save(salaEncontrada);

    }

//  Verifica se ela existe e depois remove.
    @Transactional
    public void deletarSala(Long id){

        if(salaRepository.existsById(id)){
            salaRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Sala inexistente.");
        }
    }
}
