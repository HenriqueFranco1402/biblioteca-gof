package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.pessoa.Atendente;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;

import java.util.Optional;

/**
 * Ponto interno de acesso "Relacionamento": reune GrupoDePessoas, GrupoDeAcademicos e
 * GrupoDeColaboradores do diagrama de classes.
 */
public class Relacionamento {

    private final RepositorioRelacionamento repositorio;

    public Relacionamento(RepositorioRelacionamento repositorio) {
        this.repositorio = repositorio;
    }

    public Optional<Pessoa> consultarPessoa(String cpf) {
        return repositorio.pessoaPorCpf(Pessoa.normalizarCpf(cpf));
    }

    public void inserirPessoa(Pessoa pessoa) {
        repositorio.salvarPessoa(pessoa);
    }

    public Optional<Estudante> consultarEstudante(String idEstudante) {
        return repositorio.estudante(idEstudante);
    }

    /** Usado pela fachada: o estudante precisa existir para reservar/emprestar/devolver. */
    public Estudante estudanteCadastrado(String idEstudante) {
        return consultarEstudante(idEstudante)
                .orElseThrow(() -> new RegraDeNegocioException("Estudante nao cadastrado: " + idEstudante));
    }

    public void inserirEstudante(Estudante estudante) {
        repositorio.salvarEstudante(estudante);
    }

    public Optional<Atendente> consultarAtendente(String idAtendente) {
        return repositorio.atendente(idAtendente);
    }

    public void inserirAtendente(Atendente atendente) {
        repositorio.salvarAtendente(atendente);
    }
}
