package br.pucpr.biblioteca.financeiro.cobranca;

/** Resposta institucional ao registro de uma cobranca. */
public record RegistroCobranca(String idCobranca, String referenciaNoBanco, String linhaDigitavel) {
}
