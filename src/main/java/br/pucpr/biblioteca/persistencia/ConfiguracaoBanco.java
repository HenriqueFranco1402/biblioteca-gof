package br.pucpr.biblioteca.persistencia;

/** Dados de conexao do banco relacional principal (MySQL). */
public record ConfiguracaoBanco(String url, String usuario, String senha) {

    public static ConfiguracaoBanco mysqlPadrao() {
        return new ConfiguracaoBanco(
                "jdbc:mysql://localhost:3306/biblioteca?createDatabaseIfNotExist=true&serverTimezone=UTC",
                "root", "");
    }
}
