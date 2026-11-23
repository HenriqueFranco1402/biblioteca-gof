package br.pucpr.biblioteca.persistencia.memoria;

import br.pucpr.biblioteca.dominio.pessoa.Atendente;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia memoria). */
public final class RepositorioRelacionamentoEmMemoria implements RepositorioRelacionamento {

    private final UnidadeDeTrabalhoEmMemoria uow;

    RepositorioRelacionamentoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.uow = uow;
    }

    @Override
    public Optional<Pessoa> pessoaPorCpf(String cpf) {
        return Optional.ofNullable(uow.base().pessoas.get(cpf))
                .map(r -> new Pessoa(r.cpf(), r.nome(), r.sobrenome(), r.email()));
    }

    @Override
    public void salvarPessoa(Pessoa p) {
        uow.base().pessoas.put(p.getCpfPessoa(),
                new BaseEmMemoria.PessoaRegistro(p.getCpfPessoa(), p.getNome(), p.getSobrenome(), p.getEmail()));
    }

    @Override
    public Optional<Estudante> estudante(String idEstudante) {
        return Optional.ofNullable(uow.base().estudantes.get(idEstudante))
                .map(r -> new Estudante(r.id(), r.cpf(), r.nome(), r.sobrenome(), r.email()));
    }

    @Override
    public void salvarEstudante(Estudante e) {
        uow.base().estudantes.put(e.getIdEstudante(), new BaseEmMemoria.PapelRegistro(e.getIdEstudante(),
                e.getCpfPessoa(), e.getNome(), e.getSobrenome(), e.getEmail()));
    }

    @Override
    public Optional<Atendente> atendente(String idAtendente) {
        return Optional.ofNullable(uow.base().atendentes.get(idAtendente))
                .map(r -> new Atendente(r.id(), r.cpf(), r.nome(), r.sobrenome(), r.email()));
    }

    @Override
    public void salvarAtendente(Atendente a) {
        uow.base().atendentes.put(a.getIdAtendente(), new BaseEmMemoria.PapelRegistro(a.getIdAtendente(),
                a.getCpfPessoa(), a.getNome(), a.getSobrenome(), a.getEmail()));
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }
}
