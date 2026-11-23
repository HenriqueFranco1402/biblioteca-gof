package br.pucpr.biblioteca.dominio.livro;

/**
 * PADRAO STATE - Estado concreto REPARANDO (do / reparar): saidas -> LIVRE (concluirReparo) e
 * -> INCINERADO (registrarLaudoIrrecuperavel: evento de laudo irrecuperavel).
 */
final class EstadoReparando extends EstadoLivro {
    static final EstadoReparando INSTANCIA = new EstadoReparando();

    private EstadoReparando() {
    }

    @Override
    public String nome() { return "REPARANDO"; }

    @Override
    void concluirReparo(Livro livro) {
        livro.mudarEstado(EstadoLivre.INSTANCIA);
    }

    @Override
    void registrarLaudoIrrecuperavel(Livro livro) {
        livro.mudarEstado(EstadoIncinerado.INSTANCIA);
    }
}
