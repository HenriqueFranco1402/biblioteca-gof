package br.pucpr.biblioteca.app;

import br.pucpr.biblioteca.persistencia.ConfiguracaoBanco;
import br.pucpr.biblioteca.persistencia.Perfil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuracao da instalacao. Valores de {@code biblioteca.properties} podem ser sobrescritos por
 * propriedades de sistema (-Dbiblioteca.perfil=producao) ou variaveis de ambiente (BIBLIOTECA_PERFIL).
 *
 * @param perfil         TESTE (memoria) ou PRODUCAO (MySQL) - escolhe a familia de persistencia (Abstract Factory)
 * @param banco          "A" ou "B" - escolhe o adaptador de cobranca (Adapter)
 * @param politicaMulta  nome da politica inicial (Strategy)
 * @param usarCache      liga o decorador de cache (Decorator)
 * @param usarMedicao    liga o decorador de medicao (Decorator)
 */
public record ConfiguracaoSistema(Perfil perfil, String banco, String politicaMulta,
                                  boolean usarCache, boolean usarMedicao, ConfiguracaoBanco bancoDeDados) {

    /** Configuracao usada nos testes automatizados: tudo em memoria, Banco A, politica original. */
    public static ConfiguracaoSistema paraTestes() {
        return new ConfiguracaoSistema(Perfil.TESTE, "A", "ORIGINAL", true, true, ConfiguracaoBanco.mysqlPadrao());
    }

    public ConfiguracaoSistema comBanco(String outroBanco) {
        return new ConfiguracaoSistema(perfil, outroBanco, politicaMulta, usarCache, usarMedicao, bancoDeDados);
    }

    public static ConfiguracaoSistema carregar() {
        Properties p = new Properties();
        try (InputStream in = ConfiguracaoSistema.class.getResourceAsStream("/biblioteca.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler biblioteca.properties", e);
        }
        ConfiguracaoBanco padrao = ConfiguracaoBanco.mysqlPadrao();
        return new ConfiguracaoSistema(
                Perfil.deTexto(valor(p, "biblioteca.perfil", "teste")),
                valor(p, "biblioteca.banco", "A").toUpperCase(),
                valor(p, "biblioteca.politicaMulta", "ORIGINAL"),
                Boolean.parseBoolean(valor(p, "biblioteca.consulta.cache", "true")),
                Boolean.parseBoolean(valor(p, "biblioteca.consulta.medicao", "true")),
                new ConfiguracaoBanco(
                        valor(p, "biblioteca.db.url", padrao.url()),
                        valor(p, "biblioteca.db.usuario", padrao.usuario()),
                        valor(p, "biblioteca.db.senha", padrao.senha())));
    }

    private static String valor(Properties p, String chave, String padrao) {
        String sistema = System.getProperty(chave);
        if (sistema != null) {
            return sistema;
        }
        String ambiente = System.getenv(chave.toUpperCase().replace('.', '_'));
        if (ambiente != null) {
            return ambiente;
        }
        return p.getProperty(chave, padrao);
    }
}
