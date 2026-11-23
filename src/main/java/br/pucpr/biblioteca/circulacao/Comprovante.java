package br.pucpr.biblioteca.circulacao;

import java.time.LocalDate;

/**
 * Comprovantes devolvidos pela fachada de circulacao. Sao valores imutaveis com os dados da
 * operacao concluida - nunca colecoes nem objetos de dominio - para que a interface nao consiga
 * manipular Movimentacao, Acervo ou Livro por fora da fachada.
 */
public sealed interface Comprovante {

    String resumo();

    record Reserva(String idReserva, String idLivro, String idEstudante, LocalDate data, String estadoLivro)
            implements Comprovante {
        public String resumo() {
            return "Reserva " + idReserva + ": livro " + idLivro + " reservado para " + idEstudante + " em " + data
                    + " [livro " + estadoLivro + "]";
        }
    }

    record Emprestimo(String idEmprestimo, String idLivro, String idEstudante, LocalDate dataEmprestimo,
                      LocalDate vencimento, String estadoLivro) implements Comprovante {
        public String resumo() {
            return "Emprestimo " + idEmprestimo + ": livro " + idLivro + " para " + idEstudante
                    + ", vence em " + vencimento + " [livro " + estadoLivro + "]";
        }
    }

    record Renovacao(String idEmprestimo, String idLivro, LocalDate novoVencimento, int numRenovacoes,
                     String estadoLivro) implements Comprovante {
        public String resumo() {
            return "Renovacao do emprestimo " + idEmprestimo + ": novo vencimento " + novoVencimento
                    + " (" + numRenovacoes + "/2 renovacoes) [livro " + estadoLivro + "]";
        }
    }

    record Devolucao(String idDevolucao, String idLivro, String idEstudante, LocalDate vencimento,
                     LocalDate dataDevolucao, long diasDeAtraso, String estadoLivro) implements Comprovante {
        public String resumo() {
            return "Devolucao " + idDevolucao + ": livro " + idLivro + " devolvido em " + dataDevolucao
                    + " (vencimento " + vencimento + ", " + diasDeAtraso + " dia(s) de atraso) [livro " + estadoLivro + "]";
        }
    }

    record SituacaoLivro(String idLivro, String estadoLivro, String operacao) implements Comprovante {
        public String resumo() {
            return operacao + ": livro " + idLivro + " agora esta " + estadoLivro;
        }
    }
}
