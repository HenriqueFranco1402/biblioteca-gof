package br.pucpr.biblioteca.dominio.livro;

/**
 * PADRAO STATE - Estado concreto DEVOLVIDO (entry / registrarDataDevolucao). O livro aguarda a inspecao:
 * saidas -> LIVRE (aprovarInspecao: bom estado) e -> REPARANDO (reprovarInspecao: danificado).
 * Reservar aqui e recusado pela implementacao padrao: "um livro devolvido ainda nao inspecionado
 * nao pode ser reservado".
 */
final class EstadoDevolvido extends EstadoLivro {
    static final EstadoDevolvido INSTANCIA = new EstadoDevolvido();

    private EstadoDevolvido() {
    }

    @Override
    public String nome() { return "DEVOLVIDO"; }

    @Override
    void aprovarInspecao(Livro livro) {
        livro.mudarEstado(EstadoLivre.INSTANCIA);
    }

    @Override
    void reprovarInspecao(Livro livro) {
        livro.mudarEstado(EstadoReparando.INSTANCIA);
    }
}
