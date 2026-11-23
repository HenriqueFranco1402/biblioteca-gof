package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

/** Lancada pelo passo "verificar pre-requisitos" (ex.: CPF nao cadastrado no RH). */
public class PreRequisitoNaoAtendidoException extends RegraDeNegocioException {
    public PreRequisitoNaoAtendidoException(String mensagem) {
        super(mensagem);
    }
}
