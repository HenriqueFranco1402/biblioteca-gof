package br.pucpr.biblioteca.financeiro;

import java.time.LocalDate;

/** Dados que o Financeiro precisa para emitir uma multa (contrato iMulta). */
public record SolicitacaoDeMulta(String idDevolucao, String idEstudante, String cpfEstudante,
                                 String emailEstudante, LocalDate vencimento, LocalDate dataDevolucao) {
}
