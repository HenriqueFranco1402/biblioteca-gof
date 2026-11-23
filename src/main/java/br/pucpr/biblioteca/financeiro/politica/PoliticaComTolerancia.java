package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.financeiro.Dinheiro;

/** PADRAO STRATEGY - Estrategia concreta: os 2 primeiros dias de atraso sao tolerados; depois, R$ 1,00 por dia. */
public class PoliticaComTolerancia implements PoliticaMulta {

    public static final String NOME = "TOLERANCIA";
    static final int DIAS_DE_TOLERANCIA = 2;

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public Dinheiro calcular(long diasDeAtraso) {
        return PoliticaOriginal.VALOR_POR_DIA.vezes(Math.max(0, diasDeAtraso - DIAS_DE_TOLERANCIA));
    }
}
