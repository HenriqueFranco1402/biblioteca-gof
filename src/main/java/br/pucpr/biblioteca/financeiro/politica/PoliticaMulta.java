package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.financeiro.Dinheiro;

/**
 * PADRAO STRATEGY - papel: Estrategia (interface).
 *
 * <p>Cada politica sabe apenas transformar "dias de atraso" em valor. O {@code ServicoDeMultas}
 * (contexto) nao conhece as implementacoes: recebe a politica vigente da
 * {@link ConfiguracaoAdministrativa}. Para incluir uma nova politica basta criar outra classe que
 * implemente esta interface e registra-la na configuracao - nada no servico muda.</p>
 */
public interface PoliticaMulta {

    /** Nome gravado na multa no momento da emissao. */
    String nome();

    /** @param diasDeAtraso dias corridos apos o vencimento (0 quando nao ha atraso) */
    Dinheiro calcular(long diasDeAtraso);
}
