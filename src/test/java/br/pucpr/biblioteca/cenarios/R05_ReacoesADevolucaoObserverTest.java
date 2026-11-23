package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.app.ConfiguracaoSistema;
import br.pucpr.biblioteca.app.DadosDeExemplo;
import br.pucpr.biblioteca.app.Sistema;
import br.pucpr.biblioteca.devolucao.EventoDevolucao;
import br.pucpr.biblioteca.devolucao.Indicadores;
import br.pucpr.biblioteca.devolucao.OuvinteDevolucao;
import br.pucpr.biblioteca.financeiro.Dinheiro;
import br.pucpr.biblioteca.persistencia.memoria.FabricaEmMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 5 - Reacoes independentes a devolucao (Observer). */
@DisplayName("R05 - Observer: reacoes a devolucao")
class R05_ReacoesADevolucaoObserverTest extends CenarioBase {

    @Test
    @DisplayName("R05 cenario: devolucao atrasada alimenta inspecao, multa e indicadores; livro fica DEVOLVIDO")
    void R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores() {
        reservarEEmprestar("L1", "E1");
        relogio.avancarDias(14 + 5);

        sistema.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertTrue(sistema.filaDeInspecao().contem("L1"), "inspecao");
        assertEquals(1, sistema.servicoDeMultas().multasDoEstudante("E1").size(), "multa");
        assertEquals(Dinheiro.reais(5), sistema.servicoDeMultas().multasDoEstudante("E1").get(0).getValor());
        assertEquals(new Indicadores.Resumo(1, 1, 5), sistema.indicadores().resumo(), "indicadores");
        assertEquals("DEVOLVIDO", estado("L1"), "permanece DEVOLVIDO ate a inspecao");

        sistema.tratadorDeDevolucao().registrarInspecao("L1", true);
        assertEquals("LIVRE", estado("L1"));
        assertTrue(sistema.filaDeInspecao().pendentes().isEmpty());
    }

    @Test
    @DisplayName("R05 cenario: devolucao no prazo nao gera multa")
    void R05_devolucaoNoPrazoNaoGeraMulta() {
        reservarEEmprestar("L1", "E1");
        relogio.avancarDias(10);

        sistema.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertTrue(sistema.servicoDeMultas().multasDoEstudante("E1").isEmpty());
        assertTrue(sistema.filaDeInspecao().contem("L1"));
        assertEquals(new Indicadores.Resumo(1, 0, 0), sistema.indicadores().resumo());
        assertEquals("DEVOLVIDO", estado("L1"));
    }

    @Test
    @DisplayName("R05: falha no envio de e-mail nao afeta os demais ouvintes nem desfaz a devolucao")
    void R05_falhaDoEmailNaoAfetaDemaisNemDesfazDevolucao() {
        Sistema s = new Sistema(ConfiguracaoSistema.paraTestes(), relogio,
                (para, assunto, corpo) -> { throw new IllegalStateException("servidor SMTP fora do ar"); },
                new FabricaEmMemoria());
        DadosDeExemplo.carregar(s);
        s.fachada().reservar("L1", "E1");
        s.fachada().efetivarEmprestimo("L1", "E1");
        relogio.avancarDias(14 + 2);

        s.tratadorDeDevolucao().registrarDevolucao("L1", "E1");

        assertEquals("DEVOLVIDO", s.acervo().consultarLivro("L1").getNomeEstado(), "devolucao confirmada");
        assertTrue(s.filaDeInspecao().contem("L1"));
        assertEquals(1, s.servicoDeMultas().multasDoEstudante("E1").size());
        assertEquals(1, s.indicadores().resumo().devolucoes());
        assertEquals(1, s.publicadorDevolucao().falhas().size());
        assertEquals("email", s.publicadorDevolucao().falhas().get(0).ouvinte());
    }

    @Test
    @DisplayName("R05: novo ouvinte e incluido sem alterar tratador nem fachada; o evento leva os dados")
    void R05_novoOuvinteSemAlterarTratador() {
        List<EventoDevolucao> recebidos = new ArrayList<>();
        sistema.publicadorDevolucao().registrar(new OuvinteDevolucao() {
            public String nome() { return "auditoria"; }

            public void aoDevolver(EventoDevolucao e) { recebidos.add(e); }
        });
        reservarEEmprestar("L2", "E2");
        relogio.avancarDias(20);

        sistema.tratadorDeDevolucao().registrarDevolucao("L2", "E2");

        assertEquals(1, recebidos.size());
        EventoDevolucao e = recebidos.get(0);
        assertEquals(HOJE.plusDays(14), e.vencimento());
        assertEquals(HOJE.plusDays(20), e.dataDevolucao());
        assertEquals("E2", e.idEstudante());
        assertEquals("bruno.lima@pucpr.edu.br", e.emailEstudante());
        assertEquals(6, e.diasDeAtraso());
    }
}
