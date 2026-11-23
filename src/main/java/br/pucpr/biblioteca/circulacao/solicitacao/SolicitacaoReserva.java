package br.pucpr.biblioteca.circulacao.solicitacao;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;

import java.time.Instant;

/** PADRAO COMMAND - Comando concreto: solicitacao de reserva. */
public class SolicitacaoReserva extends SolicitacaoCirculacao {

    private final String idLivro;
    private final String idEstudante;

    public SolicitacaoReserva(String id, String autor, Instant criadaEm, String idLivro, String idEstudante) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
    }

    @Override
    public Comprovante executar(FachadaCirculacao receptor) {
        return receptor.reservar(idLivro, idEstudante);
    }

    @Override
    public String descricao() {
        return "reserva do livro " + idLivro + " para " + idEstudante;
    }
}
