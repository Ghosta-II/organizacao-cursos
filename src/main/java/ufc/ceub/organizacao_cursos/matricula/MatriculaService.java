package ufc.ceub.organizacao_cursos.matricula;

import java.util.List;

import org.springframework.stereotype.Service;

import ufc.ceub.organizacao_cursos.aluno.Aluno;
import ufc.ceub.organizacao_cursos.aluno.AlunoRepository;
import ufc.ceub.organizacao_cursos.curso.Curso;
import ufc.ceub.organizacao_cursos.curso.CursoRepository;
import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Regras de negócio de matrículas.
 * Depende apenas de repositories (nunca de outros services) para evitar ciclos.
 */
@Service
public class MatriculaService {

    private final MatriculaRepository repository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;

    public MatriculaService(MatriculaRepository repository,
                            AlunoRepository alunoRepository,
                            CursoRepository cursoRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
    }

    /**
     * Matricula um aluno em um curso.
     * Aluno e curso precisam existir e o aluno não pode já estar matriculado no curso.
     */
    public MatriculaResponse matricular(MatriculaRequest request) {
        Aluno aluno = obterAlunoOuFalhar(request.alunoId());
        Curso curso = obterCursoOuFalhar(request.cursoId());
        if (repository.existsByAlunoIdAndCursoId(aluno.getId(), curso.getId())) {
            throw new RegraNegocioException("Aluno já matriculado neste curso");
        }
        Matricula matricula = new Matricula(aluno, curso);
        return MatriculaResponse.de(repository.save(matricula));
    }

    /**
     * Lista todas as matrículas cadastradas.
     */
    public List<MatriculaResponse> listar() {
        return repository.findAll().stream()
                .map(MatriculaResponse::de)
                .toList();
    }

    /**
     * Lista as matrículas de um aluno (cursos e professores do aluno).
     */
    public List<MatriculaResponse> listarPorAluno(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new RecursoNaoEncontradoException("Aluno não encontrado");
        }
        return repository.findByAlunoId(alunoId).stream()
                .map(MatriculaResponse::de)
                .toList();
    }

    /**
     * Lista as matrículas de um curso (alunos do curso).
     */
    public List<MatriculaResponse> listarPorCurso(Long cursoId) {
        if (!cursoRepository.existsById(cursoId)) {
            throw new RecursoNaoEncontradoException("Curso não encontrado");
        }
        return repository.findByCursoId(cursoId).stream()
                .map(MatriculaResponse::de)
                .toList();
    }

    /**
     * Cancela (remove) uma matrícula pelo id.
     */
    public void cancelar(Long id) {
        Matricula matricula = obterOuFalhar(id);
        repository.delete(matricula);
    }

    // Busca a entidade ou lança 404 quando o id não existe.
    private Matricula obterOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula não encontrada"));
    }

    // Busca o aluno informado na requisição ou lança 400.
    // É 400 (e não 404) porque o erro está no dado enviado pelo cliente, não no recurso acessado.
    private Aluno obterAlunoOuFalhar(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RegraNegocioException("Aluno informado não existe"));
    }

    // Busca o curso informado na requisição ou lança 400, pelo mesmo motivo acima.
    private Curso obterCursoOuFalhar(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RegraNegocioException("Curso informado não existe"));
    }
}
