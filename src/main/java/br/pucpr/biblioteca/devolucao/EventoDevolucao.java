package br.pucpr.biblioteca.devolucao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * PADRAO OBSERVER - evento publicado APOS a confirmacao da devolucao.
 *
 * <p>Leva todos os dados de que os ouvintes precisam (vencimento, data de devolucao, estudante).
 * Assim nenhum ouvinte precisa consultar Movimentacao/Relacionamento depois - o que poderia ler
 * dados ja alterados por outra operacao ou exigir acesso a componentes que nao sao dele.</p>
 */
public record EventoDevolucao(String idDevolucao, String idLivro, String tituloLivro,
                              String idEstudante, String cpfEstudante, String emailEstudante,
                              LocalDate vencimento, LocalDate dataDevolucao) {

    public long diasDeAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, dataDevolucao));
    }

    public boolean atrasada() {
        return diasDeAtraso() > 0;
    }
}
