package com.br.reservas.controller;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.dto.ReservaResponseDTO;
import com.br.reservas.model.Reserva;
import com.br.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints de reservas. Datas são enviadas no formato ISO, por exemplo {@code 2026-11-23T10:00:00}.
 */
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponseDTO> listar() {
        return reservaService.listarReservas().stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservaResponseDTO buscarPorId(@PathVariable Long id) {
        return ReservaResponseDTO.from(reservaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criar(@Valid @RequestBody ReservaRequestDTO dto) {
        Reserva reserva = reservaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponseDTO.from(reserva));
    }

    @PutMapping("/{id}")
    public ReservaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequestDTO dto) {
        return ReservaResponseDTO.from(reservaService.atualizar(id, dto));
    }

    /** Cancela a reserva. O registro continua no banco com status CANCELADA. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        reservaService.cancelar(id);
    }

    /** Reservas de qualquer sala que se sobrepõem ao período. */
    @GetMapping("/periodo")
    public List<ReservaResponseDTO> listarNoPeriodo(@RequestParam LocalDateTime inicio,
                                                    @RequestParam LocalDateTime fim) {
        return reservaService.listarNoPeriodo(inicio, fim).stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    /** Versão paginada de {@code /periodo}. Ex.: {@code ?inicio=...&fim=...&page=0&size=20&sort=inicio,asc}. */
    @GetMapping("/paginadas")
    public PagedModel<ReservaResponseDTO> listarNoPeriodoPaginado(@RequestParam LocalDateTime inicio,
                                                                  @RequestParam LocalDateTime fim,
                                                                  @PageableDefault(size = 20, sort = "inicio") Pageable pageable) {
        return new PagedModel<>(reservaService.listarNoPeriodoPaginado(inicio, fim, pageable)
                .map(ReservaResponseDTO::from));
    }

    /** Reservas de uma sala que se sobrepõem ao período, paginadas. */
    @GetMapping("/sala/{salaId}")
    public PagedModel<ReservaResponseDTO> listarPorSalaNoPeriodo(@PathVariable Long salaId,
                                                                 @RequestParam LocalDateTime inicio,
                                                                 @RequestParam LocalDateTime fim,
                                                                 @PageableDefault(size = 20, sort = "inicio") Pageable pageable) {
        return new PagedModel<>(reservaService.listarPorSalaNoPeriodo(salaId, inicio, fim, pageable)
                .map(ReservaResponseDTO::from));
    }
}
