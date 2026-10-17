package ufc.ceub.organizacao_cursos.aluno;

import java.util.List;

import org.springframework.stereotype.Service;

import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Regras de negócio de alunos.
 * Não há remoção: o histórico acadêmico é permanente.
 */
@Service
public class AlunoService {

    private final AlunoRepository repository;

    public AlunoService(AlunoRepository repository) {
        this.repository = repository;
    }

    /**
     * Cadastra um novo aluno. O RA não pode se repetir.
     */
    public AlunoResponse criar(AlunoRequest request) {
        validarRaDisponivel(request.ra());
        Aluno aluno = new Aluno(request.nome(), request.email(), request.ra());
        return AlunoResponse.de(repository.save(aluno));
    }

    /**
     * Lista todos os alunos cadastrados.
     */
    public List<AlunoResponse> listar() {
        return repository.findAll().stream()
                .map(AlunoResponse::de)
                .toList();
    }

    /**
     * Busca um aluno pelo id.
     */
    public AlunoResponse buscarPorId(Long id) {
        return AlunoResponse.de(obterOuFalhar(id));
    }

    /**
     * Atualiza nome, e-mail e RA de um aluno existente.
     * Se o RA for alterado, o novo RA também não pode se repetir.
     */
    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        Aluno aluno = obterOuFalhar(id);
        if (!aluno.getRa().equals(request.ra())) {
            validarRaDisponivel(request.ra());
        }
        aluno.setNome(request.nome());
        aluno.setEmail(request.email());
        aluno.setRa(request.ra());
        return AlunoResponse.de(repository.save(aluno));
    }

    // Busca a entidade ou lança 404 quando o id não existe.
    private Aluno obterOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado"));
    }

    // Lança 400 quando o RA já está cadastrado.
    private void validarRaDisponivel(String ra) {
        if (repository.existsByRa(ra)) {
            throw new RegraNegocioException("Já existe aluno com este RA");
        }
    }
}
