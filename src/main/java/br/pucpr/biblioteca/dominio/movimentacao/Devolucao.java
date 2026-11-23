package br.pucpr.biblioteca.dominio.movimentacao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Registro da devolucao. Guarda o vencimento vigente no momento da devolucao para o calculo do atraso. */
public class Devolucao extends Transacao {
    private final String idEmprestimo;
    private final LocalDate vencimento;

    public Devolucao(String id, String idLivro, String idEstudante, LocalDate dataDevolucao,
                     String idEmprestimo, LocalDate vencimento) {
        super(id, idLivro, idEstudante, dataDevolucao);
        this.idEmprestimo = Objects.requireNonNull(idEmprestimo);
        this.vencimento = Objects.requireNonNull(vencimento);
    }

    public LocalDate getDataDevolucao() { return getData(); }

    public String getIdEmprestimo() { return idEmprestimo; }

    public LocalDate getVencimento() { return vencimento; }

    /** Dias corridos entre o vencimento e a devolucao; nunca negativo. */
    public long diasDeAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, getDataDevolucao()));
    }
}
