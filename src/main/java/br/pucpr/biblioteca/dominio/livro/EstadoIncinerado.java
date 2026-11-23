package br.pucpr.biblioteca.dominio.livro;

/** PADRAO STATE - Estado concreto INCINERADO: estado final. Nao sobrescreve nenhum evento, logo recusa todos. */
final class EstadoIncinerado extends EstadoLivro {
    static final EstadoIncinerado INSTANCIA = new EstadoIncinerado();

    private EstadoIncinerado() {
    }

    @Override
    public String nome() { return "INCINERADO"; }
}
