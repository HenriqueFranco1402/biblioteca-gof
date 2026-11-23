package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.financeiro.Dinheiro;

/** PADRAO STRATEGY - Estrategia concreta: R$ 1,00 por dia, limitado a um teto de R$ 20,00. */
public class PoliticaComTeto implements PoliticaMulta {

    public static final String NOME = "TETO";
    static final Dinheiro TETO = Dinheiro.reais(20);

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public Dinheiro calcular(long diasDeAtraso) {
        return PoliticaOriginal.VALOR_POR_DIA.vezes(Math.max(0, diasDeAtraso)).limitadoA(TETO);
    }
}
