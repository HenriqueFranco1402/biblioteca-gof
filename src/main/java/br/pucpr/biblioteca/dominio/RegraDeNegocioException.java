package br.pucpr.biblioteca.dominio;

/** Violacao de uma regra do dominio. Toda recusa de negocio usa esta excecao (ou uma subclasse). */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
