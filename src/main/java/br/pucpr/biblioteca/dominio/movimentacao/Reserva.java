package br.pucpr.biblioteca.dominio.movimentacao;

import java.time.LocalDate;

/** Reserva ativa de um livro por um estudante. E removida quando o emprestimo e efetivado. */
public class Reserva extends Transacao {
    public Reserva(String id, String idLivro, String idEstudante, LocalDate data) {
        super(id, idLivro, idEstudante, data);
    }
}
