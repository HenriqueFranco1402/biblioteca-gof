package br.pucpr.biblioteca.persistencia.memoria;

import br.pucpr.biblioteca.persistencia.FabricaDeDados;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

/**
 * PADRAO ABSTRACT FACTORY - papel: Fabrica concreta da familia EM MEMORIA (perfil de teste).
 * Uma unica unidade de trabalho e criada e todos os repositorios ficam ligados a ela.
 */
public final class FabricaEmMemoria implements FabricaDeDados {

    private final UnidadeDeTrabalhoEmMemoria uow = new UnidadeDeTrabalhoEmMemoria(new BaseEmMemoria());
    private final RepositorioLivroEmMemoria livros = new RepositorioLivroEmMemoria(uow);
    private final RepositorioMovimentacaoEmMemoria movimentacao = new RepositorioMovimentacaoEmMemoria(uow);
    private final RepositorioRelacionamentoEmMemoria relacionamento = new RepositorioRelacionamentoEmMemoria(uow);

    @Override
    public String familia() {
        return "memoria";
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    @Override
    public RepositorioLivro repositorioLivro() {
        return livros;
    }

    @Override
    public RepositorioMovimentacao repositorioMovimentacao() {
        return movimentacao;
    }

    @Override
    public RepositorioRelacionamento repositorioRelacionamento() {
        return relacionamento;
    }
}
