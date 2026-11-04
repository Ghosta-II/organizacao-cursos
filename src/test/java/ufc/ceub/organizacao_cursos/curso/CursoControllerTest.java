package ufc.ceub.organizacao_cursos.curso;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários da camada HTTP de cursos.
 * O service é um mock: aqui se verifica status, validação e corpo da resposta.
 */
@WebMvcTest(CursoController.class)
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CursoService service;

    @Test
    @DisplayName("Caso 10 - POST /cursos válido responde 201 com professorNome")
    void deveResponder201AoCadastrar() throws Exception {
        when(service.criar(new CursoRequest("Engenharia de Software", 3600, 1L)))
                .thenReturn(new CursoResponse(1L, "Engenharia de Software", 3600, 1L, "Carlos Fodao"));

        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Engenharia de Software", "cargaHoraria": 3600, "professorId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Engenharia de Software"))
                .andExpect(jsonPath("$.cargaHoraria").value(3600))
                .andExpect(jsonPath("$.professorId").value(1))
                .andExpect(jsonPath("$.professorNome").value("Carlos Fodao"));
    }

    @Test
    @DisplayName("Caso 11 - POST /cursos com professor inexistente responde 400 com a mensagem da regra")
    void deveResponder400AoCadastrarComProfessorInexistente() throws Exception {
        when(service.criar(new CursoRequest("Engenharia de Software", 3600, 999L)))
                .thenThrow(new RegraNegocioException("Professor informado não existe"));

        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Engenharia de Software", "cargaHoraria": 3600, "professorId": 999}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Professor informado não existe"))
                .andExpect(jsonPath("$.detalhes", nullValue()));
    }

    @Test
    @DisplayName("Caso 12 - POST /cursos com carga horária negativa responde 400 com detalhe do campo")
    void deveResponder400AoCadastrarCargaHorariaNegativa() throws Exception {
        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Engenharia de Software", "cargaHoraria": -10, "professorId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", contains("cargaHoraria: carga horária deve ser positiva")));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Caso 18 - DELETE /cursos/1 com matrículas responde 400")
    void deveResponder400AoRemoverComMatriculas() throws Exception {
        doThrow(new RegraNegocioException("Curso possui matrículas ativas"))
                .when(service).remover(1L);

        mockMvc.perform(delete("/cursos/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Curso possui matrículas ativas"));
    }

    @Test
    @DisplayName("Caso 20 - DELETE /cursos/1 sem matrículas responde 204 sem corpo")
    void deveResponder204AoRemover() throws Exception {
        doNothing().when(service).remover(1L);

        mockMvc.perform(delete("/cursos/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).remover(1L);
    }
}
