package br.pucpr.biblioteca.persistencia.memoria;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * "Tabelas" da familia em memoria. Guarda REGISTROS imutaveis (records), nunca os objetos de
 * dominio vivos: assim o comportamento imita o banco relacional (o estado do Livro e gravado
 * como texto e reconstituido na leitura) e o rollback e so restaurar uma copia dos mapas.
 */
final class BaseEmMemoria {

    record LivroRegistro(String id, String titulo, String autor, String estado,
                         int numRenovacoes, LocalDate dataUltimaDevolucao) {
    }

    record TransacaoRegistro(String id, String tipo, String idLivro, String idEstudante, LocalDate data,
                             LocalDate vencimento, boolean ativo, String idEmprestimo) {
    }

    record PessoaRegistro(String cpf, String nome, String sobrenome, String email) {
    }

    record PapelRegistro(String id, String cpf, String nome, String sobrenome, String email) {
    }

    record Copia(Map<String, LivroRegistro> livros, Map<String, TransacaoRegistro> transacoes,
                 Map<String, PessoaRegistro> pessoas, Map<String, PapelRegistro> estudantes,
                 Map<String, PapelRegistro> atendentes) {
    }

    Map<String, LivroRegistro> livros = new LinkedHashMap<>();
    Map<String, TransacaoRegistro> transacoes = new LinkedHashMap<>();
    Map<String, PessoaRegistro> pessoas = new LinkedHashMap<>();
    Map<String, PapelRegistro> estudantes = new LinkedHashMap<>();
    Map<String, PapelRegistro> atendentes = new LinkedHashMap<>();

    Copia copiar() {
        return new Copia(new LinkedHashMap<>(livros), new LinkedHashMap<>(transacoes),
                new LinkedHashMap<>(pessoas), new LinkedHashMap<>(estudantes), new LinkedHashMap<>(atendentes));
    }

    void restaurar(Copia copia) {
        livros = copia.livros();
        transacoes = copia.transacoes();
        pessoas = copia.pessoas();
        estudantes = copia.estudantes();
        atendentes = copia.atendentes();
    }
}
