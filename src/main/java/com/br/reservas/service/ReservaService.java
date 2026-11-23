package com.br.reservas.service;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Reserva;
import com.br.reservas.model.Sala;
import com.br.reservas.model.TipoReserva;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import com.br.reservas.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de reserva: conflito de horário, sala ativa e cancelamento.
 *
 * <p>Criação e alteração rodam em transação e travam a linha da sala antes da checagem de conflito.
 * Sem o bloqueio, dois pedidos simultâneos poderiam ver o horário livre e gravar os dois.</p>
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository,
                          SalaRepository salaRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva inexistente."));
    }

    @Transactional
    public Reserva criar(ReservaRequestDTO dto) {
        Reserva.validarPeriodo(dto.getInicio(), dto.getFim());

        Sala sala = buscarSalaComBloqueio(dto.getSalaId());
        Usuario usuario = buscarUsuario(dto.getUsuarioId());

        // Construir a reserva antes da checagem faz as regras de domínio (ex.: sala inativa) valerem primeiro.
        Reserva nova = new Reserva(dto.getInicio(), dto.getFim(), sala, usuario);

        garantirHorarioLivre(sala.getId(), dto.getInicio(), dto.getFim(), null);

        return reservaRepository.save(nova);
    }

    @Transactional
    public Reserva atualizar(Long id, ReservaRequestDTO dto) {
        Reserva reserva = buscarPorId(id);
        reserva.garantirNaoCancelada();
        Reserva.validarPeriodo(dto.getInicio(), dto.getFim());

        Sala sala = buscarSalaComBloqueio(dto.getSalaId());
        Usuario usuario = buscarUsuario(dto.getUsuarioId());

        // Ignora a própria reserva, senão ela conflitaria com ela mesma.
        garantirHorarioLivre(sala.getId(), dto.getInicio(), dto.getFim(), id);

        reserva.atualizar(dto.getInicio(), dto.getFim(), sala, usuario);
        return reservaRepository.save(reserva);
    }

    /** Cancela a reserva. Não remove o registro, para manter o histórico. */
    @Transactional
    public void cancelar(Long id) {
        Reserva reserva = buscarPorId(id);
        reserva.cancelar();
        reservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarNoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Reserva.validarPeriodo(inicio, fim);
        return reservaRepository.buscarNoPeriodo(inicio, fim);
    }

    @Transactional(readOnly = true)
    public Page<Reserva> listarNoPeriodoPaginado(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        Reserva.validarPeriodo(inicio, fim);
        return reservaRepository.buscarNoPeriodoPaginado(inicio, fim, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Reserva> listarPorSalaNoPeriodo(Long salaId, LocalDateTime inicio, LocalDateTime fim,
                                                Pageable pageable) {
        Reserva.validarPeriodo(inicio, fim);
        buscarSala(salaId);
        return reservaRepository.buscarPorSalaNoPeriodo(salaId, inicio, fim, pageable);
    }

    /**
     * Verifica se a sala está livre no período.
     * Reservas canceladas não entram na checagem. Com {@code idIgnorado} (alteração),
     * a própria reserva também é ignorada.
     */
    private void garantirHorarioLivre(Long salaId, LocalDateTime inicio, LocalDateTime fim, Long idIgnorado) {
        boolean ocupado = idIgnorado == null
                ? reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
                        salaId, TipoReserva.ATIVA, fim, inicio)
                : reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
                        salaId, TipoReserva.ATIVA, fim, inicio, idIgnorado);

        if (ocupado) {
            throw new ConflitoException("O horário da reserva precisa estar disponível.");
        }
    }

    private Sala buscarSalaComBloqueio(Long salaId) {
        return salaRepository.buscarComBloqueio(salaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));
    }

    private Sala buscarSala(Long salaId) {
        return salaRepository.findById(salaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala inexistente."));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário inexistente."));
    }
}
