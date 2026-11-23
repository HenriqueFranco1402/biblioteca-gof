package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;
import br.pucpr.biblioteca.devolucao.FilaDeInspecao;

import java.util.List;

/**
 * Tratador de devolucao (diagrama de classes: registrarDevolucao, emitirBoletoMulta).
 * A emissao da multa/boleto acontece pelos ouvintes da devolucao (Observer); este tratador nao
 * precisa conhecer nenhum deles.
 */
public class TratadorDeDevolucao {

    private final FachadaCirculacao fachada;
    private final FilaDeInspecao filaDeInspecao;

    public TratadorDeDevolucao(FachadaCirculacao fachada, FilaDeInspecao filaDeInspecao) {
        this.fachada = fachada;
        this.filaDeInspecao = filaDeInspecao;
    }

    public Comprovante.Devolucao registrarDevolucao(String idLivro, String idEstudante) {
        return fachada.registrarDevolucao(idLivro, idEstudante);
    }

    public Comprovante.SituacaoLivro registrarInspecao(String idLivro, boolean bomEstado) {
        return fachada.registrarInspecao(idLivro, bomEstado);
    }

    public Comprovante.SituacaoLivro enviarParaReparo(String idLivro) {
        return fachada.enviarParaReparo(idLivro);
    }

    public Comprovante.SituacaoLivro concluirReparo(String idLivro) {
        return fachada.concluirReparo(idLivro);
    }

    public Comprovante.SituacaoLivro registrarLaudoIrrecuperavel(String idLivro) {
        return fachada.registrarLaudoIrrecuperavel(idLivro);
    }

    public List<String> livrosAguardandoInspecao() {
        return filaDeInspecao.pendentes();
    }
}
