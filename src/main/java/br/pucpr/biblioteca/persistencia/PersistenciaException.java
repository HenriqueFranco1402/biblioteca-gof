package br.pucpr.biblioteca.persistencia;

/** Falha de infraestrutura de persistencia (ex.: SQLException), separada das regras de negocio. */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
