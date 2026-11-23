package com.br.reservas.controller;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.dto.ReservaResponseDTO;
import com.br.reservas.model.Reserva;
import com.br.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponseDTO> listar() {

        List<Reserva> reservas = reservaService.listarReservas();
        List<ReservaResponseDTO> reservasDTO = new ArrayList<>();

        for (Reserva reserva : reservas) {
            reservasDTO.add(new ReservaResponseDTO(reserva.getId(), reserva.getUsuario().getId(), reserva.getSala().getId(),
                    reserva.getInicio(), reserva.getFim(), reserva.getTipoReserva()));
        }

        return reservasDTO;
    }

    @GetMapping("/{id}")
    public ReservaResponseDTO listarPorId(@PathVariable Long id) {

        Reserva reserva = reservaService.listarPorId(id);
        return new ReservaResponseDTO(reserva.getId(), reserva.getUsuario().getId(), reserva.getSala().getId(), reserva.getInicio(),
                reserva.getFim(), reserva.getTipoReserva());
    }

    @PostMapping
    public Reserva adicionar(@Valid @RequestBody ReservaRequestDTO reservaDTO) {
        return reservaService.salvarReserva(reservaDTO);
    }

    @PutMapping("/{id}")
    public Reserva atualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequestDTO reservaDTO) {
        return reservaService.atualizarReserva(id, reservaDTO);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
    }

    @GetMapping("/periodo")
    public List<ReservaResponseDTO> listarReservasPorPeriodo(@RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim){

        List<Reserva> reservas = reservaService.listarReservasPorPeriodo(inicio, fim);
        List<ReservaResponseDTO> reservasDTO = new ArrayList<>();
        for (Reserva reserva : reservas) {
            reservasDTO.add(new ReservaResponseDTO(reserva.getId(), reserva.getUsuario().getId(),
                    reserva.getSala().getId(), reserva.getInicio(), reserva.getFim(), reserva.getTipoReserva()));
        }
        return reservasDTO;
    }

    @GetMapping("/paginadas")
    public Page<ReservaResponseDTO> listarReservasPaginadas(@RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                                             Pageable pageable) {

        Page<Reserva> reservas = reservaService.listarReservasPaginadas(inicio, fim, pageable);
        Page<ReservaResponseDTO> reservasDTO = reservas.map(r -> new ReservaResponseDTO(r.getId(),
                r.getUsuario().getId(), r.getSala().getId(),
                r.getInicio(), r.getFim(), r.getTipoReserva()));
        return reservasDTO;
    }

}
