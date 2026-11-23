package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;
import br.pucpr.biblioteca.circulacao.solicitacao.ExecutorDeSolicitacoes;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoCirculacao;

/**
 * Tratador de emprestimo (diagrama de classes: solicitarReservaLivro, realizarEmprestimoLivro,
 * renovarEmprestimo). As telas criam as SOLICITACOES (Command) e as entregam aqui; o tratador
 * encaminha ao executor (Invocador). A efetivacao do emprestimo vai direto a fachada.
 */
public class TratadorDeEmprestimo {

    private final ExecutorDeSolicitacoes executor;
    private final FachadaCirculacao fachada;

    public TratadorDeEmprestimo(ExecutorDeSolicitacoes executor, FachadaCirculacao fachada) {
        this.executor = executor;
        this.fachada = fachada;
    }

    /** solicitarReservaLivro / renovarEmprestimo: recebe o comando pronto. */
    public Comprovante submeter(SolicitacaoCirculacao solicitacao) {
        return executor.executar(solicitacao);
    }

    /** realizarEmprestimoLivro. */
    public Comprovante.Emprestimo realizarEmprestimo(String idLivro, String idEstudante) {
        return fachada.efetivarEmprestimo(idLivro, idEstudante);
    }
}
