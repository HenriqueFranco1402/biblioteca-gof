package br.pucpr.biblioteca.persistencia;

import java.util.function.Supplier;

/**
 * PADRAO ABSTRACT FACTORY - papel: Produto abstrato "unidade de trabalho" (transacao).
 *
 * <p>Todos os repositorios criados por uma mesma fabrica compartilham a MESMA instancia de
 * unidade de trabalho; por isso uma operacao da fachada que grava em Livro, Movimentacao e
 * Relacionamento e confirmada ou desfeita por inteiro.</p>
 */
public interface UnidadeDeTrabalho {

    void iniciar();

    void confirmar();

    void desfazer();

    boolean emAndamento();

    /**
     * Executa a operacao dentro de uma transacao: confirma no sucesso e desfaz em qualquer
     * excecao. Se ja houver uma transacao em andamento, a operacao participa dela.
     */
    default <T> T executar(Supplier<T> operacao) {
        if (emAndamento()) {
            return operacao.get();
        }
        iniciar();
        try {
            T resultado = operacao.get();
            confirmar();
            return resultado;
        } catch (RuntimeException | Error falha) {
            desfazer();
            throw falha;
        }
    }
}
