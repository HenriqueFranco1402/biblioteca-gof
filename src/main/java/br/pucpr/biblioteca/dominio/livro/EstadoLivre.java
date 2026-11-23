package br.pucpr.biblioteca.dominio.livro;

/** PADRAO STATE - Estado concreto LIVRE: saidas -> RESERVADO (reservar) e -> REPARANDO (enviarParaReparo). */
final class EstadoLivre extends EstadoLivro {
    static final EstadoLivre INSTANCIA = new EstadoLivre();

    private EstadoLivre() {
    }

    @Override
    public String nome() { return "LIVRE"; }

    @Override
    void reservar(Livro livro) {
        livro.mudarEstado(EstadoReservado.INSTANCIA);
    }

    @Override
    void enviarParaReparo(Livro livro) {
        livro.mudarEstado(EstadoReparando.INSTANCIA);
    }
}
