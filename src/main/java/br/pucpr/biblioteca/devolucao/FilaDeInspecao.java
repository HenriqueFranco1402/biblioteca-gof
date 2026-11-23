package br.pucpr.biblioteca.devolucao;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Livros devolvidos que aguardam a decisao da inspecao (o livro fica DEVOLVIDO ate la). */
public class FilaDeInspecao {

    private final Set<String> pendentes = new LinkedHashSet<>();

    public synchronized void incluir(String idLivro) {
        pendentes.add(idLivro);
    }

    public synchronized void concluir(String idLivro) {
        pendentes.remove(idLivro);
    }

    public synchronized boolean contem(String idLivro) {
        return pendentes.contains(idLivro);
    }

    public synchronized List<String> pendentes() {
        return List.copyOf(pendentes);
    }
}
