package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;

import java.time.LocalDate;

/** PADRAO STATE - Estado concreto EMPRESTADO: saidas -> RENOVADO (renovar [numRenovacoes &lt; 2]) e -> DEVOLVIDO. */
final class EstadoEmprestado extends EstadoLivro {
    static final EstadoEmprestado INSTANCIA = new EstadoEmprestado();

    private EstadoEmprestado() {
    }

    @Override
    public String nome() { return "EMPRESTADO"; }

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
