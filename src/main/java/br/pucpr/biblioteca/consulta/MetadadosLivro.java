package br.pucpr.biblioteca.consulta;

/**
 * Metadados de um livro (titulo, autor). NAO contem o estado: e isso que o cache pode guardar,
 * porque metadados mudam raramente e nao autorizam nenhuma operacao de circulacao.
 */
public record MetadadosLivro(String idLivro, String titulo, String autor) {
}
