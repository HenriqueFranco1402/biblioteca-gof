package br.pucpr.biblioteca.persistencia;

import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.movimentacao.Transacao;

import java.util.List;
import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto abstrato: repositorio das transacoes (usado pelo ponto de acesso Movimentacao). */
public interface RepositorioMovimentacao {

    void adicionar(Transacao transacao);

    void atualizar(Emprestimo emprestimo);

    void remover(Reserva reserva);

    Optional<Reserva> reservaAtiva(String idLivro);

    Optional<Emprestimo> emprestimoAtivo(String idLivro);

    Optional<Devolucao> ultimaDevolucao(String idLivro, String idEstudante);

    List<Transacao> listar();

    UnidadeDeTrabalho unidadeDeTrabalho();
}
