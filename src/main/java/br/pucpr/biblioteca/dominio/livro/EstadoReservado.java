package br.pucpr.biblioteca.dominio.livro;

/** PADRAO STATE - Estado concreto RESERVADO: saida -> EMPRESTADO (emprestar). */
final class EstadoReservado extends EstadoLivro {
    static final EstadoReservado INSTANCIA = new EstadoReservado();

    private EstadoReservado() {
    }

    @Override
    public String nome() { return "RESERVADO"; }

    @Override
    void emprestar(Livro livro) {
        livro.zerarRenovacoes();          // acao: um novo emprestimo comeca sem renovacoes
        livro.mudarEstado(EstadoEmprestado.INSTANCIA);
    }
}
