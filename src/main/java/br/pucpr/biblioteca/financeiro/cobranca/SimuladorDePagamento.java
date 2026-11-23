package br.pucpr.biblioteca.financeiro.cobranca;

import java.time.LocalDate;

/**
 * Apenas para a demonstracao: faz o banco simulado "pagar" uma cobranca, disparando a notificacao
 * no formato proprietario do banco (que o adaptador traduz). Nao faz parte do contrato institucional.
 */
public interface SimuladorDePagamento {
    void simularPagamento(String idCobranca, LocalDate data);
}
