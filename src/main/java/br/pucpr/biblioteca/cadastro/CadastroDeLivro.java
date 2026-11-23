package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/** PADRAO TEMPLATE METHOD - Classe concreta: cadastro de Livro (nao sobrescreve o gancho de pre-requisitos). */
public class CadastroDeLivro extends CadastroTemplate<Dados.Livro, Livro> {

    private final Acervo acervo;

    public CadastroDeLivro(UnidadeDeTrabalho uow, Acervo acervo) {
        super(uow);
        this.acervo = acervo;
    }

    @Override
    protected Optional<Livro> buscarExistente(Dados.Livro d) {
        return d.idLivro() == null ? Optional.empty() : acervo.buscar(d.idLivro());
    }

    @Override
    protected void validar(Dados.Livro d) {
        exigir(d.idLivro(), "idLivro");
        exigir(d.titulo(), "titulo");
    }

    @Override
    protected Livro criar(Dados.Livro d) {
        return Livro.novo(d.idLivro(), d.titulo(), d.autor());
    }

    @Override
    protected void armazenar(Livro livro) {
        acervo.inserir(livro);
    }

    @Override
    protected ResultadoCadastro apresentar(Livro livro, boolean novo) {
        return new ResultadoCadastro(livro.getIdLivro(), novo, livro.toString());
    }
}
