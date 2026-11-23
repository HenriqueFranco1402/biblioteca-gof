package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;

import java.time.LocalDate;

/** PADRAO STATE - Estado concreto RENOVADO: saidas -> RENOVADO (auto-transicao renovar [numRenovacoes &lt; 2]) e -> DEVOLVIDO. */
final class EstadoRenovado extends EstadoLivro {
    static final EstadoRenovado INSTANCIA = new EstadoRenovado();

    private EstadoRenovado() {
    }

    @Override
    public String nome() { return "RENOVADO"; }

    @Override
    void renovar(Livro livro, Emprestimo emprestimo) {
        Renovacao.aplicar(livro, emprestimo);
    }

    @Override
    void devolver(Livro livro, LocalDate dataDevolucao) {
        livro.registrarDataDevolucao(dataDevolucao);   // entry / registrarDataDevolucao de DEVOLVIDO
        livro.mudarEstado(EstadoDevolvido.INSTANCIA);
    }
}
