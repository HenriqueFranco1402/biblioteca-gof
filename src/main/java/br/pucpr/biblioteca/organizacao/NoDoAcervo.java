package br.pucpr.biblioteca.organizacao;

import java.util.List;

/**
 * PADRAO COMPOSITE - papel: Componente. Grupos (unidade, secao, subsecao) e itens respondem as
 * MESMAS operacoes de contagem e listagem, entao quem usa a arvore nao precisa saber se tem nas
 * maos uma folha ou um grupo.
 */
public abstract class NoDoAcervo {

    private final String nome;
    private GrupoDoAcervo pai;

    protected NoDoAcervo(String nome) {
        this.nome = nome;
    }

    /** Quantidade de livros abaixo deste no (o proprio item conta 1). */
    public abstract int contarLivros();

    /** Ids dos livros abaixo deste no, em ordem de insercao. */
    public abstract List<String> listarLivros();

    public String getNome() {
        return nome;
    }

    public GrupoDoAcervo getPai() {
        return pai;
    }

    void definirPai(GrupoDoAcervo pai) {
        this.pai = pai;
    }

    /** Caminho legivel, ex.: "Campus Curitiba > Exatas > Computacao". */
    public String caminho() {
        return pai == null ? nome : pai.caminho() + " > " + nome;
    }
}
