package br.pucpr.biblioteca.financeiro;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Multa emitida. O VALOR e o NOME DA POLITICA sao gravados na emissao e nunca recalculados:
 * trocar a politica vigente depois nao altera multas ja emitidas.
 */
public class Multa {

    public enum Situacao { PENDENTE, QUITADA }

    private final String idMulta;
    private final String idDevolucao;
    private final String idEstudante;
    private final long diasDeAtraso;
    private final Dinheiro valor;
    private final String politicaAplicada;
    private final LocalDate dataEmissao;
    private Situacao situacao = Situacao.PENDENTE;
    private LocalDate dataQuitacao;
    private String referenciaBancaria;

    public Multa(String idMulta, String idDevolucao, String idEstudante, long diasDeAtraso,
                 Dinheiro valor, String politicaAplicada, LocalDate dataEmissao) {
        this.idMulta = Objects.requireNonNull(idMulta);
        this.idDevolucao = idDevolucao;
        this.idEstudante = idEstudante;
        this.diasDeAtraso = diasDeAtraso;
        this.valor = Objects.requireNonNull(valor);
        this.politicaAplicada = Objects.requireNonNull(politicaAplicada);
        this.dataEmissao = dataEmissao;
    }

    /** @return true se a multa passou de PENDENTE para QUITADA; false se ja estava quitada. */
    synchronized boolean quitar(LocalDate data) {
        if (situacao == Situacao.QUITADA) {
            return false;
        }
        situacao = Situacao.QUITADA;
        dataQuitacao = data;
        return true;
    }

    void vincularReferenciaBancaria(String referencia) {
        this.referenciaBancaria = referencia;
    }

    public String getIdMulta() { return idMulta; }

    public String getIdDevolucao() { return idDevolucao; }

    public String getIdEstudante() { return idEstudante; }

    public long getDiasDeAtraso() { return diasDeAtraso; }

    public Dinheiro getValor() { return valor; }

    public String getPoliticaAplicada() { return politicaAplicada; }

    public LocalDate getDataEmissao() { return dataEmissao; }

    public synchronized Situacao getSituacao() { return situacao; }

    public synchronized LocalDate getDataQuitacao() { return dataQuitacao; }

    public String getReferenciaBancaria() { return referenciaBancaria; }

    @Override
    public String toString() {
        return idMulta + " " + valor + " (" + diasDeAtraso + " dia(s), politica " + politicaAplicada + ") " + getSituacao();
    }
}
