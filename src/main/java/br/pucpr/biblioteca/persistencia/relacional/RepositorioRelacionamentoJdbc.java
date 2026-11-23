package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.dominio.pessoa.Atendente;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia relacional / MySQL). */
public final class RepositorioRelacionamentoJdbc implements RepositorioRelacionamento {

    private final UnidadeDeTrabalhoJdbc uow;
    private final Jdbc jdbc;

    RepositorioRelacionamentoJdbc(UnidadeDeTrabalhoJdbc uow) {
        this.uow = uow;
        this.jdbc = new Jdbc(uow);
    }

    @Override
    public Optional<Pessoa> pessoaPorCpf(String cpf) {
        return jdbc.primeiro("SELECT * FROM pessoa WHERE cpf = ?", rs -> new Pessoa(rs.getString("cpf"),
                rs.getString("nome"), rs.getString("sobrenome"), rs.getString("email")), cpf);
    }

    @Override
    public void salvarPessoa(Pessoa p) {
        jdbc.atualizarOuInserir("UPDATE pessoa SET nome = ?, sobrenome = ?, email = ? WHERE cpf = ?",
                new Object[]{p.getNome(), p.getSobrenome(), p.getEmail(), p.getCpfPessoa()},
                "INSERT INTO pessoa (cpf, nome, sobrenome, email) VALUES (?, ?, ?, ?)",
                new Object[]{p.getCpfPessoa(), p.getNome(), p.getSobrenome(), p.getEmail()});
    }

    @Override
    public Optional<Estudante> estudante(String idEstudante) {
        return jdbc.primeiro("SELECT * FROM estudante WHERE id_estudante = ?", rs -> new Estudante(
                rs.getString("id_estudante"), rs.getString("cpf"), rs.getString("nome"),
                rs.getString("sobrenome"), rs.getString("email")), idEstudante);
    }

    @Override
    public void salvarEstudante(Estudante e) {
        jdbc.atualizarOuInserir("UPDATE estudante SET cpf = ?, nome = ?, sobrenome = ?, email = ? WHERE id_estudante = ?",
                new Object[]{e.getCpfPessoa(), e.getNome(), e.getSobrenome(), e.getEmail(), e.getIdEstudante()},
                "INSERT INTO estudante (id_estudante, cpf, nome, sobrenome, email) VALUES (?, ?, ?, ?, ?)",
                new Object[]{e.getIdEstudante(), e.getCpfPessoa(), e.getNome(), e.getSobrenome(), e.getEmail()});
    }

    @Override
    public Optional<Atendente> atendente(String idAtendente) {
        return jdbc.primeiro("SELECT * FROM atendente WHERE id_atendente = ?", rs -> new Atendente(
                rs.getString("id_atendente"), rs.getString("cpf"), rs.getString("nome"),
                rs.getString("sobrenome"), rs.getString("email")), idAtendente);
    }

    @Override
    public void salvarAtendente(Atendente a) {
        jdbc.atualizarOuInserir("UPDATE atendente SET cpf = ?, nome = ?, sobrenome = ?, email = ? WHERE id_atendente = ?",
                new Object[]{a.getCpfPessoa(), a.getNome(), a.getSobrenome(), a.getEmail(), a.getIdAtendente()},
                "INSERT INTO atendente (id_atendente, cpf, nome, sobrenome, email) VALUES (?, ?, ?, ?, ?)",
                new Object[]{a.getIdAtendente(), a.getCpfPessoa(), a.getNome(), a.getSobrenome(), a.getEmail()});
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }
}
