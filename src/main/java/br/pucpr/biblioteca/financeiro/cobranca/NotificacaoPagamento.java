package br.pucpr.biblioteca.financeiro.cobranca;

import java.time.LocalDate;

/** Notificacao de pagamento no formato INSTITUCIONAL (produzida pelos adaptadores). */
public record NotificacaoPagamento(String idCobranca, SituacaoPagamento situacao, LocalDate dataPagamento) {

    public enum SituacaoPagamento { PAGO, CANCELADO, OUTRA }

    public boolean pago() {
        return situacao == SituacaoPagamento.PAGO;
    }
}
