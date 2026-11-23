package br.pucpr.biblioteca.persistencia.memoria;

import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.List;
import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia memoria). */
public final class RepositorioLivroEmMemoria implements RepositorioLivro {

    private final UnidadeDeTrabalhoEmMemoria uow;

    /** Exige a unidade de trabalho DA MESMA FAMILIA: nao ha como liga-lo a uma transacao JDBC. */
    RepositorioLivroEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.uow = uow;
    }

    @Override
    public Optional<Livro> buscar(String idLivro) {
        return Optional.ofNullable(uow.base().livros.get(idLivro)).map(RepositorioLivroEmMemoria::paraDominio);
    }

    @Override
    public void salvar(Livro l) {
        uow.base().livros.put(l.getIdLivro(), new BaseEmMemoria.LivroRegistro(l.getIdLivro(), l.getTitulo(),
                l.getAutor(), l.getNomeEstado(), l.getNumRenovacoes(), l.getDataUltimaDevolucao()));
    }

    @Override
    public List<Livro> listar() {
        return uow.base().livros.values().stream().map(RepositorioLivroEmMemoria::paraDominio).toList();
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    private static Livro paraDominio(BaseEmMemoria.LivroRegistro r) {
        return Livro.reconstituir(r.id(), r.titulo(), r.autor(), r.estado(), r.numRenovacoes(), r.dataUltimaDevolucao());
    }
}
