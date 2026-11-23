package br.pucpr.biblioteca.devolucao;

/** PADRAO OBSERVER - Observador concreto: coloca o livro devolvido na fila de inspecao. */
public class OuvinteInspecao implements OuvinteDevolucao {

    private final FilaDeInspecao fila;

    public OuvinteInspecao(FilaDeInspecao fila) {
        this.fila = fila;
    }

    @Override
    public String nome() {
        return "inspecao";
    }

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        fila.incluir(evento.idLivro());
    }
}
