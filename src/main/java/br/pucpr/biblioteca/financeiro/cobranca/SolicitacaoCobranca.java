package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.financeiro.Dinheiro;

import java.time.LocalDate;

/**
 * Formato INSTITUCIONAL de uma cobranca: valor em {@link Dinheiro}, CPF so com digitos e
 * identificador institucional (o id da multa).
 */
public record SolicitacaoCobranca(String idCobranca, String cpfPagador, Dinheiro valor, LocalDate vencimento) {
}
