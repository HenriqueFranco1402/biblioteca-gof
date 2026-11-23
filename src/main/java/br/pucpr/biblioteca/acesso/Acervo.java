package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;

import java.util.List;
import java.util.Optional;

/**
 * Ponto interno de acesso "Acervo" (Trabalho 02 / AcervoDeLivros no diagrama de classes).
 * Encapsula o repositorio de Livro; quem esta fora do componente Biblioteca nao ve o repositorio.
 */
public class Acervo {

    private final RepositorioLivro repositorio;

    public Acervo(RepositorioLivro repositorio) {
        this.repositorio = repositorio;
    }

    /** consultarLivro(idLivro) do diagrama de classes. */
    public Livro consultarLivro(String idLivro) {
        return buscar(idLivro).orElseThrow(() -> new RegraDeNegocioException("Livro nao cadastrado: " + idLivro));
    }

    public Optional<Livro> buscar(String idLivro) {
        return repositorio.buscar(idLivro);
    }

    public void inserir(Livro livro) {
        repositorio.salvar(livro);
    }

    /** Grava o livro (inclusive o novo estado, pelo nome). */
    public void atualizar(Livro livro) {
        repositorio.salvar(livro);
    }

    public List<Livro> listar() {
        return repositorio.listar();
    }
}
