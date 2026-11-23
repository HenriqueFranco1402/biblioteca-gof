package br.pucpr.biblioteca.circulacao;

import java.util.UUID;

/** Geracao de identificadores de transacoes (unicos mesmo entre reinicios com o banco relacional). */
public final class Ids {
    private Ids() {
    }

    public static String novo(String prefixo) {
        return prefixo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
