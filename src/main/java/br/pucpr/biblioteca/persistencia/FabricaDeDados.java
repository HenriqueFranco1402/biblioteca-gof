package br.pucpr.biblioteca.persistencia;

/**
 * PADRAO ABSTRACT FACTORY - papel: Fabrica abstrata.
 *
 * <p>Cria uma FAMILIA coerente de objetos de acesso a dados: a unidade de trabalho e os
 * repositorios de Livro, Movimentacao e Relacionamento. Ha duas fabricas concretas:
 * {@code FabricaEmMemoria} (testes) e {@code FabricaRelacional} (producao, MySQL).</p>
 *
 * <p>Como cada fabrica concreta so sabe criar produtos da sua propria familia, e como os
 * repositorios concretos exigem no construtor a unidade de trabalho concreta da mesma familia,
 * nao e possivel misturar um repositorio em memoria com outro relacional.</p>
 */
public interface FabricaDeDados {

    /** Nome da familia ("memoria" ou "relacional"). */
    String familia();

    UnidadeDeTrabalho unidadeDeTrabalho();

    RepositorioLivro repositorioLivro();

    RepositorioMovimentacao repositorioMovimentacao();

    RepositorioRelacionamento repositorioRelacionamento();

    /** Preparacao da familia (ex.: criar tabelas). Por padrao, nada. */
    default void inicializar() {
    }
}
