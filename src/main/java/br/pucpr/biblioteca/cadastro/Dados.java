package br.pucpr.biblioteca.cadastro;

/** Dados de entrada dos formularios de cadastro. */
public final class Dados {
    private Dados() {
    }

    public record Livro(String idLivro, String titulo, String autor) {
    }

    public record Pessoa(String cpf, String nome, String sobrenome, String email) {
    }

    public record Estudante(String idEstudante, String cpf) {
    }

    public record Atendente(String idAtendente, String cpf) {
    }
}
