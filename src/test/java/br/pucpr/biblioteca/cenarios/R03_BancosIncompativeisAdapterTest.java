package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.app.RelogioAjustavel;
import br.pucpr.biblioteca.banco.BancoAApi;
import br.pucpr.biblioteca.banco.BancoBApi;
import br.pucpr.biblioteca.financeiro.Dinheiro;
import br.pucpr.biblioteca.financeiro.Multa;
import br.pucpr.biblioteca.financeiro.ServicoDeMultas;
import br.pucpr.biblioteca.financeiro.SolicitacaoDeMulta;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoA;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoB;
import br.pucpr.biblioteca.financeiro.cobranca.GatewayCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.SolicitacaoCobranca;
import br.pucpr.biblioteca.financeiro.politica.ConfiguracaoAdministrativa;
import br.pucpr.biblioteca.notificacao.CaixaDeSaidaEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 3 - Integracao com bancos incompativeis (Adapter). */
@DisplayName("R03 - Adapter: bancos incompativeis")
class R03_BancosIncompativeisAdapterTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);

    @Test
    @DisplayName("R03 cenario: a mesma solicitacao de R$ 7,00 vira 700 centavos no Banco A e 7.00 no Banco B")
    void R03_seteReaisViram700CentavosNoBancoAE7ReaisNoBancoB() {
        BancoAApi bancoA = new BancoAApi();
        BancoBApi bancoB = new BancoBApi();
        SolicitacaoCobranca mesma = new SolicitacaoCobranca("MUL-1", "11144477735", Dinheiro.reais("7.00"), HOJE);

        new AdaptadorBancoA(bancoA).registrar(mesma);
        new AdaptadorBancoB(bancoB).registrar(mesma);

        assertEquals(700L, bancoA.ultimoValorEmCentavos());
        assertEquals(new BigDecimal("7.00"), bancoB.ultimoValor());
    }

    @Test
    @DisplayName("R03: o mesmo ServicoDeMultas funciona com qualquer banco, sem condicionais por banco")
    void R03_servicoDeMultasIgualParaOsDoisBancos() throws Exception {
        BancoAApi bancoA = new BancoAApi();
        BancoBApi bancoB = new BancoBApi();
        for (GatewayCobranca gateway : new GatewayCobranca[]{new AdaptadorBancoA(bancoA), new AdaptadorBancoB(bancoB)}) {
            ServicoDeMultas servico = new ServicoDeMultas(new ConfiguracaoAdministrativa(), gateway,
                    new CaixaDeSaidaEmail(), RelogioAjustavel.em(HOJE));
            servico.emitirMulta(new SolicitacaoDeMulta("DEV-1", "E1", "11144477735", "e1@pucpr.br",
                    HOJE.minusDays(7), HOJE));   // 7 dias de atraso -> R$ 7,00
        }
        assertEquals(700L, bancoA.ultimoValorEmCentavos());
        assertEquals(new BigDecimal("7.00"), bancoB.ultimoValor());

        // prova estrutural: o codigo do servico nao cita nenhum banco concreto
        String fonte = Files.readString(Path.of("src/main/java/br/pucpr/biblioteca/financeiro/ServicoDeMultas.java"));
        assertFalse(fonte.contains("BancoA") || fonte.contains("BancoB") || fonte.contains("AdaptadorBanco"),
                "ServicoDeMultas nao pode depender de banco concreto");
    }

    @Test
    @DisplayName("R03: notificacao de pagamento repetida nao gera segunda quitacao (Banco A e Banco B)")
    void R03_notificacaoRepetidaNaoGeraSegundaQuitacao() {
        BancoAApi apiA = new BancoAApi();
        AdaptadorBancoA adaptadorA = new AdaptadorBancoA(apiA);
        BancoBApi apiB = new BancoBApi();
        AdaptadorBancoB adaptadorB = new AdaptadorBancoB(apiB);

        for (var par : new Object[][]{{adaptadorA}, {adaptadorB}}) {
            GatewayCobranca gateway = (GatewayCobranca) par[0];
            ServicoDeMultas servico = new ServicoDeMultas(new ConfiguracaoAdministrativa(), gateway,
                    new CaixaDeSaidaEmail(), RelogioAjustavel.em(HOJE));
            Multa multa = servico.emitirMulta(new SolicitacaoDeMulta("DEV-1", "E1", "11144477735", "e1@pucpr.br",
                    HOJE.minusDays(3), HOJE)).orElseThrow();

            var simulador = (br.pucpr.biblioteca.financeiro.cobranca.SimuladorDePagamento) gateway;
            simulador.simularPagamento(multa.getIdMulta(), HOJE);   // 1a notificacao
            simulador.simularPagamento(multa.getIdMulta(), HOJE);   // repetida

            assertEquals(Multa.Situacao.QUITADA, multa.getSituacao());
            assertEquals(1, servico.quitacoesEfetivas(), "banco " + gateway.banco() + ": so uma quitacao");
        }
    }

    @Test
    @DisplayName("R03: o adaptador converte documento e identificador para o formato de cada banco")
    void R03_conversaoDeDocumentos() {
        BancoBApi apiB = new BancoBApi();
        var registro = new AdaptadorBancoB(apiB).registrar(
                new SolicitacaoCobranca("MUL-9", "11144477735", Dinheiro.reais(3), HOJE));
        assertTrue(registro.referenciaNoBanco().startsWith("B-"));
        assertEquals("MUL-9", registro.idCobranca());

        var registroA = new AdaptadorBancoA(new BancoAApi()).registrar(
                new SolicitacaoCobranca("MUL-9", "11144477735", Dinheiro.reais(3), HOJE));
        assertTrue(registroA.referenciaNoBanco().startsWith("A-"));
    }
}
