package br.pucpr.biblioteca.circulacao.solicitacao;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;

import java.time.Instant;

/** PADRAO COMMAND - Comando concreto: solicitacao de renovacao (+30 dias, sujeita a guarda numRenovacoes &lt; 2). */
public class SolicitacaoRenovacao extends SolicitacaoCirculacao {

    private final String idLivro;
    private final String idEstudante;

    public SolicitacaoRenovacao(String id, String autor, Instant criadaEm, String idLivro, String idEstudante) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    @Override
    public Comprovante executar(FachadaCirculacao receptor) {
        return receptor.renovar(idLivro, idEstudante);
    }

    @Override
    public String descricao() {
        return "renovacao do livro " + idLivro + " para " + idEstudante;
    }
}
