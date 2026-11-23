package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.financeiro.Dinheiro;
import br.pucpr.biblioteca.financeiro.Multa;
import br.pucpr.biblioteca.financeiro.politica.PoliticaComTeto;
import br.pucpr.biblioteca.financeiro.politica.PoliticaComTolerancia;
import br.pucpr.biblioteca.financeiro.politica.PoliticaMulta;
import br.pucpr.biblioteca.financeiro.politica.PoliticaOriginal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 2 - Politicas de calculo de multa (Strategy). */
@DisplayName("R02 - Strategy: politicas de multa")
class R02_PoliticasDeMultaStrategyTest extends CenarioBase {

    @Test
    @DisplayName("R02 cenario: 5 dias -> R$ 5,00 (original) e R$ 3,00 (tolerancia); sem atraso -> zero")
    void R02_cincoDiasOriginalCincoReaisToleranciaTresReaisSemAtrasoZero() {
        PoliticaMulta original = new PoliticaOriginal();
        PoliticaMulta tolerancia = new PoliticaComTolerancia();
        PoliticaMulta teto = new PoliticaComTeto();

        assertEquals(Dinheiro.reais("5.00"), original.calcular(5));
        assertEquals(Dinheiro.reais("3.00"), tolerancia.calcular(5));
        for (PoliticaMulta p : List.of(original, tolerancia, teto)) {
            assertEquals(Dinheiro.ZERO, p.calcular(0), p.nome() + " sem atraso deve ser zero");
        }
        assertEquals(Dinheiro.reais("20.00"), teto.calcular(35), "teto de R$ 20,00");
        assertEquals(Dinheiro.ZERO, tolerancia.calcular(2), "2 dias estao dentro da tolerancia");
    }

    @Test
    @DisplayName("R02: politica vigente vem da configuracao administrativa e a multa grava valor e politica")
    void R02_multaGravaValorEPoliticaETrocaNaoRecalculaEmitidas() {
        // devolucao com 5 dias de atraso na politica ORIGINAL
        reservarEEmprestar("L1", "E1");
        relogio.avancarDias(14 + 5);
        sistema.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        // a administracao troca a politica
        sistema.tratadorFinanceiro().definirPoliticaMulta("TOLERANCIA");
        reservarEEmprestar("L2", "E2");
        relogio.avancarDias(14 + 5);
        sistema.tratadorDeDevolucao().registrarDevolucao("L2", "E2");

        Multa primeira = sistema.servicoDeMultas().multasDoEstudante("E1").get(0);
        Multa segunda = sistema.servicoDeMultas().multasDoEstudante("E2").get(0);
        assertEquals(Dinheiro.reais("5.00"), primeira.getValor(), "multa ja emitida NAO e recalculada");
        assertEquals("ORIGINAL", primeira.getPoliticaAplicada());
        assertEquals(Dinheiro.reais("3.00"), segunda.getValor());
        assertEquals("TOLERANCIA", segunda.getPoliticaAplicada());
    }

    @Test
    @DisplayName("R02: nova politica entra sem alterar o servico de multas")
    void R02_novaPoliticaSemAlterarServico() {
        // "politica de meio real por dia" criada aqui mesmo, so implementando a interface
        PoliticaMulta meioReal = new PoliticaMulta() {
            public String nome() { return "MEIO_REAL"; }

            public Dinheiro calcular(long dias) { return new Dinheiro(50).vezes(dias); }
        };
        sistema.configuracaoAdministrativa().registrar(meioReal);
        sistema.tratadorFinanceiro().definirPoliticaMulta("MEIO_REAL");

        reservarEEmprestar("L1", "E1");
        relogio.avancarDias(14 + 4);
        sistema.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        Multa m = sistema.servicoDeMultas().multasDoEstudante("E1").get(0);
        assertEquals(Dinheiro.reais("2.00"), m.getValor());
        assertTrue(sistema.tratadorFinanceiro().politicasDisponiveis().contains("MEIO_REAL"));
    }
}
