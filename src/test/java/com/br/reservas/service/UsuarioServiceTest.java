package com.br.reservas.service;

import com.br.reservas.dto.UsuarioRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.DadosInvalidosException;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ReservaRepository reservaRepository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, reservaRepository);
    }

    private UsuarioRequestDTO dto(String nome, String email) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        return dto;
    }

    @Test
    void criar_comEmailNovo_salvaUsuarioComEmailNormalizado() {
        when(usuarioRepository.existsByEmail("ana@exemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = service.criar(dto("Ana", "  Ana@Exemplo.com "));

        assertThat(criado.getEmail()).isEqualTo("ana@exemplo.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void criar_comEmailJaCadastrado_lancaConflitoENaoSalva() {
        when(usuarioRepository.existsByEmail("ana@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(dto("Ana", "ana@exemplo.com")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("E-mail já cadastrado.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizar_paraEmailDeOutroUsuario_lancaConflito() {
        Usuario existente = new Usuario("Ana", "ana@exemplo.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailAndIdNot(eq("bruno@exemplo.com"), eq(1L))).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, dto("Ana", "bruno@exemplo.com")))
                .isInstanceOf(ConflitoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void excluir_usuarioComReservas_lancaConflitoENaoRemove() {
        Usuario usuario = new Usuario("Ana", "ana@exemplo.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(reservaRepository.existsByUsuario_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Usuário possui reservas e não pode ser excluído.");
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    void criar_comNomeAusenteNoDto_lancaDadosInvalidos() {
        // Bean Validation barra isso antes, na API. Aqui conferimos a regra de domínio sem o controller.
        assertThatThrownBy(() -> service.criar(dto(null, "ana@exemplo.com")))
                .isInstanceOf(DadosInvalidosException.class);
        verify(usuarioRepository, never()).save(any());
    }
}
