package br.pucpr.biblioteca.devolucao;

/**
 * PADRAO OBSERVER - papel: Observador (interface). Cada reacao independente a uma devolucao
 * (inspecao, multa, indicadores, e-mail...) implementa esta interface e e registrada no
 * {@link PublicadorDevolucao}. Um novo ouvinte e incluido sem alterar o tratador nem a fachada.
 */
public interface OuvinteDevolucao {

    /** Nome usado nos registros de falha. */
    String nome();

    void aoDevolver(EventoDevolucao evento);
}
