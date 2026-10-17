package ufc.ceub.organizacao_cursos.matricula;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints REST de matrículas.
 */
@RestController
@RequestMapping("/matriculas")
@Tag(name = "Matrículas")
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Matricula um aluno em um curso")
    public MatriculaResponse matricular(@Valid @RequestBody MatriculaRequest request) {
        return service.matricular(request);
    }

    @GetMapping
    @Operation(summary = "Lista todas as matrículas")
    public List<MatriculaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/aluno/{alunoId}")
    @Operation(summary = "Lista os cursos (e professores) em que o aluno está matriculado")
    public List<MatriculaResponse> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @GetMapping("/curso/{cursoId}")
    @Operation(summary = "Lista os alunos matriculados no curso")
    public List<MatriculaResponse> listarPorCurso(@PathVariable Long cursoId) {
        return service.listarPorCurso(cursoId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancela uma matrícula pelo id")
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }
}
