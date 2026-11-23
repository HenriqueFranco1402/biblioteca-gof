package br.pucpr.biblioteca.organizacao;

import java.util.List;

/**
 * PADRAO COMPOSITE - papel: Folha. Apenas REFERENCIA o livro pelo identificador; nao guarda o objeto
 * Livro. Por isso mover o item entre grupos nao toca no Livro (estado) nem na Movimentacao (emprestimo).
 */
public final class ItemDoAcervo extends NoDoAcervo {

    private final String idLivro;

    ItemDoAcervo(String idLivro) {
        super("livro " + idLivro);
        this.idLivro = idLivro;
    }

    public String getIdLivro() {
        return idLivro;
    }

    @Override
    public int contarLivros() {
        return 1;
    }

    @Override
    public List<String> listarLivros() {
        return List.of(idLivro);
    }
}
