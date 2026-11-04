package ufc.ceub.organizacao_cursos.matricula;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários da camada HTTP de matrículas.
 * O service é um mock: aqui se verifica status e corpo da resposta.
 */
@WebMvcTest(MatriculaController.class)
class MatriculaControllerTest {

    private static final String MATRICULA_VALIDA = """
            {"alunoId": 1, "cursoId": 1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatriculaService service;

    @Test
    @DisplayName("Caso 14 - POST /matriculas válido responde 201 com aluno, curso e professor")
    void deveResponder201AoMatricular() throws Exception {
        when(service.matricular(new MatriculaRequest(1L, 1L))).thenReturn(matriculaResponse());

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MATRICULA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alunoId").value(1))
                .andExpect(jsonPath("$.alunoNome").value("Ana Souza"))
                .andExpect(jsonPath("$.cursoId").value(1))
                .andExpect(jsonPath("$.cursoNome").value("Engenharia de Software"))
                .andExpect(jsonPath("$.professorNome").value("Carlos Fodao"))
                .andExpect(jsonPath("$.dataMatricula").value("2026-11-04"));
    }

    @Test
    @DisplayName("Caso 15 - POST /matriculas repetido responde 400 com a mensagem da regra")
    void deveResponder400AoMatricularRepetido() throws Exception {
        when(service.matricular(new MatriculaRequest(1L, 1L)))
                .thenThrow(new RegraNegocioException("Aluno já matriculado neste curso"));

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MATRICULA_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Aluno já matriculado neste curso"))
                .andExpect(jsonPath("$.detalhes", nullValue()));
    }

    @Test
    @DisplayName("Caso 16 - POST /matriculas com aluno inexistente responde 400 com a mensagem da regra")
    void deveResponder400AoMatricularAlunoInexistente() throws Exception {
        when(service.matricular(new MatriculaRequest(999L, 1L)))
                .thenThrow(new RegraNegocioException("Aluno informado não existe"));

        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alunoId": 999, "cursoId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Aluno informado não existe"))
                .andExpect(jsonPath("$.detalhes", nullValue()));
    }

    @Test
    @DisplayName("Caso 17 - GET /matriculas/aluno/1 responde 200 com lista de curso e professor")
    void deveResponder200AoListarPorAluno() throws Exception {
        when(service.listarPorAluno(1L)).thenReturn(List.of(matriculaResponse()));

        mockMvc.perform(get("/matriculas/aluno/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cursoNome").value("Engenharia de Software"))
                .andExpect(jsonPath("$[0].professorNome").value("Carlos Fodao"));
    }

    @Test
    @DisplayName("Caso 19 - DELETE /matriculas/1 responde 204 sem corpo")
    void deveResponder204AoCancelar() throws Exception {
        doNothing().when(service).cancelar(1L);

        mockMvc.perform(delete("/matriculas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).cancelar(1L);
    }

    // Monta a resposta de uma matrícula com os dados usados nos casos do handoff.
    private MatriculaResponse matriculaResponse() {
        return new MatriculaResponse(1L, 1L, "Ana Souza", 1L, "Engenharia de Software",
                "Carlos Fodao", LocalDate.of(2026, 11, 4));
    }
}
