package br.pucpr.biblioteca.dominio.movimentacao;

import java.time.LocalDate;
import java.util.Objects;

/** Emprestimo de um livro. O vencimento e prorrogado pela transicao "renovar" da maquina de estados. */
public class Emprestimo extends Transacao {
    public static final int PRAZO_INICIAL_DIAS = 14;

    private LocalDate dataVencimento;
    private boolean ativo;

    public Emprestimo(String id, String idLivro, String idEstudante, LocalDate data,
                      LocalDate dataVencimento, boolean ativo) {
        super(id, idLivro, idEstudante, data);
        this.dataVencimento = Objects.requireNonNull(dataVencimento);
        this.ativo = ativo;
    }

    /** Novo emprestimo com o prazo inicial padrao. */
    public static Emprestimo novo(String id, String idLivro, String idEstudante, LocalDate data) {
        return new Emprestimo(id, idLivro, idEstudante, data, data.plusDays(PRAZO_INICIAL_DIAS), true);
    }

    /** Operacao do diagrama de classes: atualizarDataVencimento(dias:int). */
    public void atualizarDataVencimento(int dias) {
        this.dataVencimento = dataVencimento.plusDays(dias);
    }

    public void encerrar() {
        this.ativo = false;
    }

    public LocalDate getDataVencimento() { return dataVencimento; }

    public boolean isAtivo() { return ativo; }
}
