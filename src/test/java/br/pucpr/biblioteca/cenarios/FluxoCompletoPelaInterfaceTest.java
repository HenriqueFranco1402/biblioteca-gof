package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.interfacecli.TelaTerminal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fluxo completo da demonstracao, executado pela interface de linha de comando (roteiro-demo.txt). */
@DisplayName("Fluxo completo pela interface: reserva, emprestimo, renovacao, devolucao atrasada e multa")
class FluxoCompletoPelaInterfaceTest extends CenarioBase {

    @Test
    void fluxoCompletoDoRoteiroDeDemonstracao() throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        TelaTerminal tela = new TelaTerminal(sistema, relogio, new PrintStream(saida, true, StandardCharsets.UTF_8));

        try (BufferedReader roteiro = Files.newBufferedReader(Path.of("roteiro-demo.txt"), StandardCharsets.UTF_8)) {
            tela.executar(roteiro, true);
        }
        String texto = saida.toString(StandardCharsets.UTF_8);

        assertTrue(texto.contains("reservado para E1"), texto);
        assertTrue(texto.contains("[livro EMPRESTADO]"));
        assertTrue(texto.contains("(1/2 renovacoes)"));
        assertTrue(texto.contains("(2/2 renovacoes)"));
        assertTrue(texto.contains("RECUSADO: Renovacao recusada"));
        assertTrue(texto.contains("5 dia(s) de atraso"));
        assertTrue(texto.contains("MUL-1 R$ 5,00 (5 dia(s), politica ORIGINAL) PENDENTE"));
        assertTrue(texto.contains("RECUSADO: Operacao 'reservar' nao permitida: livro L1 esta DEVOLVIDO"));
        assertTrue(texto.contains("MUL-1 R$ 5,00 (5 dia(s), politica ORIGINAL) QUITADA"));
        assertEquals(1, sistema.servicoDeMultas().quitacoesEfetivas(), "pagamento repetido nao quita duas vezes");
        assertEquals("LIVRE", estado("L1"));
    }
}
