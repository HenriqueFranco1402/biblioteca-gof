package br.pucpr.biblioteca.app;

import br.pucpr.biblioteca.interfacecli.TelaTerminal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ponto de entrada. Uso:
 * <pre>
 *   mvnw exec:java                         (interativo)
 *   mvnw exec:java -Dexec.args=demo.txt    (executa um roteiro de comandos)
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.carregar();
        RelogioAjustavel relogio = RelogioAjustavel.agora();
        Sistema sistema = new Sistema(configuracao, relogio);
        System.out.println("Familia de persistencia: " + sistema.fabrica().familia()
                + " (perfil " + configuracao.perfil() + ")");
        if (sistema.tratadorDeAcervo().listarLivros().isEmpty()) {
            DadosDeExemplo.carregar(sistema);
        }

        TelaTerminal tela = new TelaTerminal(sistema, relogio, System.out);
        if (args.length > 0) {
            try (BufferedReader roteiro = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
                tela.executar(roteiro, true);
            }
        } else {
            tela.executar(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), false);
        }
    }
}
