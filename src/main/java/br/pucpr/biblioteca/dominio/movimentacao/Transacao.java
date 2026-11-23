package br.pucpr.biblioteca.dominio.movimentacao;

import java.time.LocalDate;
import java.util.Objects;

/** Superclasse das transacoes guardadas pela Movimentacao (diagrama de classes: Reserva, Emprestimo, Devolucao). */
public abstract class Transacao {
    private final String id;
    private final String idLivro;
    private final String idEstudante;
    private final LocalDate data;

    protected Transacao(String id, String idLivro, String idEstudante, LocalDate data) {
        this.id = Objects.requireNonNull(id);
        this.idLivro = Objects.requireNonNull(idLivro);
        this.idEstudante = Objects.requireNonNull(idEstudante);
        this.data = Objects.requireNonNull(data);
    }

    public String getId() { return id; }

    public String getIdLivro() { return idLivro; }

    public String getIdEstudante() { return idEstudante; }

    public LocalDate getData() { return data; }

    public boolean pertenceA(String idEstudante) {
        return this.idEstudante.equals(idEstudante);
    }
}
