package com.br.reservas.service;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.exception.ReservaConflitoException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Reserva;
import com.br.reservas.model.Sala;
import com.br.reservas.model.TipoReserva;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import com.br.reservas.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository, SalaRepository salaRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
    }

    private boolean existeConflitoDeHorario(Long salaId, LocalDateTime inicio, LocalDateTime fim) {
        return reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
                salaId,
                TipoReserva.ATIVA,
                fim,
                inicio
        );
    }
    //Primeiro verifica se o horário está livre, depois grava a reserva. As etapas devem fazer parte da mesma transação.
    @Transactional
    public Reserva salvarReserva(ReservaRequestDTO dto) {

        Sala sala = salaRepository.findById(dto.getSalaId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário inexistente."));

        if (!sala.isAtiva()) {
            throw new IllegalArgumentException("Sala inativa.");
        }

        if (existeConflitoDeHorario(dto.getSalaId(), dto.getInicio(), dto.getFim())) {
            throw new ReservaConflitoException("O horário da reserva precisa estar disponível.");
        }

        Reserva reserva = new Reserva(dto.getInicio(), dto.getFim(), sala, usuario);

        return reservaRepository.save(reserva);
    }

    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    public Reserva listarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva inexistente."));
    }

//  Muda o estado da reserva, e essa mudança precisa ser salva no banco.
    @Transactional
    public void cancelarReserva(Long id){

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva inexistente."));

        reserva.cancelar();
        reservaRepository.save(reserva);
    }

//  Verifica se a nova data e sala geram conflito, depois altera a reserva. A verificação e a alteração devem ser tratadas juntas.
    @Transactional
    public Reserva atualizarReserva(Long id, ReservaRequestDTO dto) {

        Reserva reservaAtual = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva inexistente."));
        Sala sala = salaRepository.findById(dto.getSalaId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário inexistente."));

        if (!sala.isAtiva()) {
            throw new IllegalArgumentException("Sala inativa.");
        }

        if (reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
                sala.getId(),
                TipoReserva.ATIVA,
                dto.getFim(),
                dto.getInicio(),
                id
        )) {
            throw new ReservaConflitoException("O horário da reserva precisa estar disponível.");
        }

        reservaAtual.setInicio(dto.getInicio());
        reservaAtual.setFim(dto.getFim());
        reservaAtual.setSala(sala);
        reservaAtual.setUsuario(usuario);

        return reservaRepository.save(reservaAtual);
    }

    public List<Reserva> listarReservasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return reservaRepository.findByInicioBetween(inicio, fim);
    }

    public Page<Reserva> listarReservasPaginadas(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return reservaRepository.findByInicioBetween(inicio, fim, pageable);
    }


}
