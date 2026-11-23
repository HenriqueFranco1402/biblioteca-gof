package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.movimentacao.Transacao;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;

import java.util.Optional;

/**
 * Ponto interno de acesso "Movimentacao" (diagrama de classes: Movimentacao com a colecao de transacoes).
 * As operacoes seguem os diagramas de sequencia (consultarReserva, removerReserva, adicionar,
 * consultarEmprestimo, consultarDevolucao). Nenhuma colecao e exposta para fora.
 */
public class Movimentacao {

    private final RepositorioMovimentacao repositorio;

    public Movimentacao(RepositorioMovimentacao repositorio) {
        this.repositorio = repositorio;
    }

    /** 1.1 consultarReserva(idLivro, idEstudante) - diagrama "Emprestar Livro". */
    public Reserva consultarReserva(String idLivro, String idEstudante) {
        return repositorio.reservaAtiva(idLivro)
                .filter(r -> r.pertenceA(idEstudante))
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Livro nao reservado: nao ha reserva ativa do livro " + idLivro + " para o estudante " + idEstudante));
    }

    public Optional<Reserva> reservaAtiva(String idLivro) {
        return repositorio.reservaAtiva(idLivro);
    }

    /** 1.3 removerReserva(reserva). */
    public void removerReserva(Reserva reserva) {
        repositorio.remover(reserva);
    }

    /** adicionar(transacao): reserva, emprestimo ou devolucao. */
    public void adicionar(Transacao transacao) {
        repositorio.adicionar(transacao);
    }

    /** consultarEmprestimo(idLivro, idEstudante) - diagramas de renovacao e devolucao. */
    public Emprestimo consultarEmprestimo(String idLivro, String idEstudante) {
        return repositorio.emprestimoAtivo(idLivro)
                .filter(e -> e.pertenceA(idEstudante))
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Nao ha emprestimo ativo do livro " + idLivro + " para o estudante " + idEstudante));
    }

    public Optional<Emprestimo> emprestimoAtivo(String idLivro) {
        return repositorio.emprestimoAtivo(idLivro);
    }

    public void atualizar(Emprestimo emprestimo) {
        repositorio.atualizar(emprestimo);
    }

    /** consultarDevolucao(idLivro, idEstudante) - diagrama "EmitirBoletoMulta". */
    public Devolucao consultarDevolucao(String idLivro, String idEstudante) {
        return repositorio.ultimaDevolucao(idLivro, idEstudante)
                .orElseThrow(() -> new RegraDeNegocioException("Devolucao nao encontrada"));
    }

    public int totalDeTransacoes() {
        return repositorio.listar().size();
    }
}
