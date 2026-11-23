package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;

/**
 * Corpo da transicao "renovar [numRenovacoes &lt; 2] / vencimento += 30 dias", compartilhado
 * pelos estados EMPRESTADO e RENOVADO (as duas transicoes do diagrama tem guarda e acao iguais).
 *
 * <p>A GUARDA e avaliada ANTES de qualquer alteracao: se ela for falsa, a excecao sai daqui
 * sem que o vencimento, o contador ou o estado tenham sido tocados.</p>
 */
final class Renovacao {
    static final int DIAS_POR_RENOVACAO = 30;

    private Renovacao() {
    }

    static void aplicar(Livro livro, Emprestimo emprestimo) {
        // 1) guarda [numRenovacoes < 2]
        if (livro.getNumRenovacoes() >= Livro.LIMITE_RENOVACOES) {
            throw new TransicaoInvalidaException("Renovacao recusada: o livro " + livro.getIdLivro()
                    + " ja foi renovado " + Livro.LIMITE_RENOVACOES + " vezes (guarda numRenovacoes < 2)");
        }
        // 2) acoes da transicao
        emprestimo.atualizarDataVencimento(DIAS_POR_RENOVACAO);
        livro.incrementarRenovacoes();
        // 3) estado de destino
        livro.mudarEstado(EstadoRenovado.INSTANCIA);
    }
}
