package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

/** Lancada quando um evento chega a um estado que nao o aceita, ou quando uma guarda e falsa. */
public class TransicaoInvalidaException extends RegraDeNegocioException {
    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
