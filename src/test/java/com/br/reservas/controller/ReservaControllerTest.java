package com.br.reservas.controller;

import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.DadosInvalidosException;
import com.br.reservas.exception.RegraDeNegocioException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Reserva;
import com.br.reservas.model.Sala;
import com.br.reservas.model.Usuario;
import com.br.reservas.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o contrato HTTP: códigos de status e formato dos erros.
 * O serviço é substituído por um mock, então as regras de negócio não são executadas aqui.
 */
@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    private static final String JSON_VALIDO = """
            {"usuarioId": 1, "salaId": 1,
             "inicio": "2026-11-23T10:00:00", "fim": "2026-11-23T11:00:00"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReservaService reservaService;

    @Test
    void criar_comDadosValidos_devolve201ComReserva() throws Exception {
        Sala sala = new Sala("Sala 101", 10);
        Usuario usuario = new Usuario("Ana", "ana@exemplo.com");
        Reserva criada = new Reserva(LocalDateTime.of(2026, 11, 23, 10, 0),
                LocalDateTime.of(2026, 11, 23, 11, 0), sala, usuario);
        when(reservaService.criar(any())).thenReturn(criada);

        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoReserva").value("ATIVA"));
    }

    @Test
    void criar_semCampoObrigatorio_devolve400ComCampoProblematico() throws Exception {
        String semSala = """
                {"usuarioId": 1, "inicio": "2026-11-23T10:00:00", "fim": "2026-11-23T11:00:00"}
                """;

        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semSala))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."))
                .andExpect(jsonPath("$.campos.salaId").value("ID da sala é obrigatório."));
    }

    @Test
    void criar_comFimAntesDoInicio_devolve400() throws Exception {
        when(reservaService.criar(any()))
                .thenThrow(new DadosInvalidosException("O fim precisa ser depois do início."));

        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O fim precisa ser depois do início."));
    }

    @Test
    void criar_comHorarioOcupado_devolve409() throws Exception {
        when(reservaService.criar(any()))
                .thenThrow(new ConflitoException("O horário da reserva precisa estar disponível."));

        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("O horário da reserva precisa estar disponível."));
    }

    @Test
    void criar_emSalaInativa_devolve422() throws Exception {
        when(reservaService.criar(any()))
                .thenThrow(new RegraDeNegocioException("Sala inativa não pode receber reservas."));

        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void buscarPorId_inexistente_devolve404() throws Exception {
        when(reservaService.buscarPorId(999L))
                .thenThrow(new ResourceNotFoundException("Reserva inexistente."));

        mvc.perform(get("/api/v1/reservas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Reserva inexistente."));
    }

    @Test
    void buscarPorId_comIdNaoNumerico_devolve400() throws Exception {
        mvc.perform(get("/api/v1/reservas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.id").exists());
    }

    @Test
    void periodo_semParametroFim_devolve400() throws Exception {
        mvc.perform(get("/api/v1/reservas/periodo")
                        .param("inicio", "2026-11-23T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.fim").value("Parâmetro obrigatório."));
    }

    @Test
    void periodo_comDataEmFormatoInvalido_devolve400() throws Exception {
        mvc.perform(get("/api/v1/reservas/periodo")
                        .param("inicio", "23/11/2026")
                        .param("fim", "2026-11-23T11:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.inicio").exists());
    }

    @Test
    void cancelar_devolve204SemCorpo() throws Exception {
        mvc.perform(delete("/api/v1/reservas/1"))
                .andExpect(status().isNoContent());

        verify(reservaService).cancelar(1L);
    }

    @Test
    void paginadas_comSortInvalido_devolve400ComCampoSort() throws Exception {
        mvc.perform(get("/api/v1/reservas/paginadas")
                        .param("inicio", "2026-12-01T00:00:00")
                        .param("fim", "2026-12-02T00:00:00")
                        .param("sort", "campoInexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.sort").exists());
    }

    @Test
    void paginadas_comTamanhoAcimaDoLimite_devolve400ComCampoSize() throws Exception {
        mvc.perform(get("/api/v1/reservas/paginadas")
                        .param("inicio", "2026-12-01T00:00:00")
                        .param("fim", "2026-12-02T00:00:00")
                        .param("size", "1000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.size").exists());
    }

    @Test
    void paginadas_comPaginaEmFormatoInvalido_devolve400ComCampoPage() throws Exception {
        mvc.perform(get("/api/v1/reservas/paginadas")
                        .param("inicio", "2026-12-01T00:00:00")
                        .param("fim", "2026-12-02T00:00:00")
                        .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.page").exists());
    }

    @Test
    void paginadas_comParametrosValidos_devolve200() throws Exception {
        when(reservaService.listarNoPeriodoPaginado(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/api/v1/reservas/paginadas")
                        .param("inicio", "2026-12-01T00:00:00")
                        .param("fim", "2026-12-02T00:00:00")
                        .param("sort", "inicio,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaInexistente_devolve404NoFormatoPadrao() throws Exception {
        mvc.perform(get("/api/v1/naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void metodoNaoSuportado_devolve405NoFormatoPadrao() throws Exception {
        mvc.perform(patch("/api/v1/reservas/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void tipoDeConteudoErrado_devolve415NoFormatoPadrao() throws Exception {
        mvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(JSON_VALIDO))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void erroInesperado_devolve500SemVazarDetalhes() throws Exception {
        when(reservaService.buscarPorId(1L))
                .thenThrow(new IllegalStateException("detalhe interno que não deve vazar"));

        mvc.perform(get("/api/v1/reservas/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor."));
    }

    @Test
    void cancelar_inexistente_devolve404() throws Exception {
        doThrow(new ResourceNotFoundException("Reserva inexistente."))
                .when(reservaService).cancelar(999L);

        mvc.perform(delete("/api/v1/reservas/999"))
                .andExpect(status().isNotFound());
    }
}
