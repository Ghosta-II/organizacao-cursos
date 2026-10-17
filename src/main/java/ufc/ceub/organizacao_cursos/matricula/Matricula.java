package ufc.ceub.organizacao_cursos.matricula;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import ufc.ceub.organizacao_cursos.aluno.Aluno;
import ufc.ceub.organizacao_cursos.curso.Curso;

/**
 * Entidade que representa a matrícula de um aluno em um curso.
 * Um aluno só pode estar matriculado uma única vez em cada curso.
 */
@Entity
@Table(name = "matriculas", uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "curso_id"}))
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aluno matriculado (obrigatório).
    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    // Curso em que o aluno foi matriculado (obrigatório).
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    // Data em que a matrícula foi realizada.
    @Column(name = "data_matricula", nullable = false)
    private LocalDate dataMatricula;

    // Construtor vazio exigido pelo JPA.
    protected Matricula() {
    }

    public Matricula(Aluno aluno, Curso curso) {
        this.aluno = aluno;
        this.curso = curso;
        // A data da matrícula é sempre a data atual no momento da criação.
        this.dataMatricula = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Curso getCurso() {
        return curso;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }
}
