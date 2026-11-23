package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.financeiro.Dinheiro;

/** PADRAO STRATEGY - Estrategia concreta: politica original, R$ 1,00 por dia de atraso. */
public class PoliticaOriginal implements PoliticaMulta {

    public static final String NOME = "ORIGINAL";
    static final Dinheiro VALOR_POR_DIA = Dinheiro.reais(1);

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public Dinheiro calcular(long diasDeAtraso) {
        return VALOR_POR_DIA.vezes(Math.max(0, diasDeAtraso));
    }
}
