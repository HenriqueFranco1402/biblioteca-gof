package br.pucpr.biblioteca.persistencia;

/** Perfil de execucao. Define, uma unica vez, qual fabrica de dados sera usada. */
public enum Perfil {
    /** Familia em memoria: testes automatizados e demonstracao sem banco. */
    TESTE,
    /** Familia relacional: MySQL (banco principal). */
    PRODUCAO;

    public static Perfil deTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return TESTE;
        }
        return switch (texto.trim().toLowerCase()) {
            case "producao", "produção", "prod", "production" -> PRODUCAO;
            case "teste", "test", "memoria", "dev" -> TESTE;
            default -> throw new IllegalArgumentException("Perfil desconhecido: " + texto);
        };
    }
}
