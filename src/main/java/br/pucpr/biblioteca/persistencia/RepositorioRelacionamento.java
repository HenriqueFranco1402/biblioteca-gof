package br.pucpr.biblioteca.persistencia;

import br.pucpr.biblioteca.dominio.pessoa.Atendente;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;

import java.util.Optional;

/**
 * PADRAO ABSTRACT FACTORY - Produto abstrato: repositorio de pessoas, estudantes e atendentes
 * (usado pelo ponto de acesso Relacionamento; corresponde a GrupoDePessoas, GrupoDeAcademicos e
 * GrupoDeColaboradores do diagrama de classes).
 */
public interface RepositorioRelacionamento {

    Optional<Pessoa> pessoaPorCpf(String cpf);

    void salvarPessoa(Pessoa pessoa);

    Optional<Estudante> estudante(String idEstudante);

    void salvarEstudante(Estudante estudante);

    Optional<Atendente> atendente(String idAtendente);

    void salvarAtendente(Atendente atendente);

    UnidadeDeTrabalho unidadeDeTrabalho();
}
