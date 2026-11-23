package br.pucpr.biblioteca.persistencia;

import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.List;
import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto abstrato: repositorio de Livro (usado pelo ponto de acesso Acervo). */
public interface RepositorioLivro {

    Optional<Livro> buscar(String idLivro);

    /** Insere ou atualiza. O estado e gravado pelo nome (ex.: "EMPRESTADO"). */
    void salvar(Livro livro);

    List<Livro> listar();

    /** Unidade de trabalho a que este repositorio esta ligado. */
    UnidadeDeTrabalho unidadeDeTrabalho();
}
