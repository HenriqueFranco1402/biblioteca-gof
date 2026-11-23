package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Requisito 1 - Ciclo de vida do livro (State). */
@DisplayName("R01 - State: ciclo de vida do livro")
class R01_CicloDeVidaDoLivroStateTest extends CenarioBase {

    @Test
    @DisplayName("R01 cenario: terceira renovacao e recusada sem alterar o vencimento")
    void R01_terceiraRenovacaoRecusada() {
        reservarEEmprestar("L1", "E1");
        renovar("L1", "E1");
        renovar("L1", "E1");
        LocalDate vencimentoAntes = vencimento("L1");

        assertThrows(TransicaoInvalidaException.class, () -> renovar("L1", "E1"));

        assertEquals(vencimentoAntes, vencimento("L1"), "o vencimento nao pode mudar");
        assertEquals(HOJE.plusDays(14 + 30 + 30), vencimento("L1"));
        assertEquals(2, sistema.acervo().consultarLivro("L1").getNumRenovacoes());
        assertEquals("RENOVADO", estado("L1"));
    }

    @Test
    @DisplayName("R01 cenario: livro devolvido ainda nao inspecionado nao pode ser reservado")
    void R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado() {
        reservarEEmprestar("L1", "E1");
        sistema.tratadorDeDevolucao().registrarDevolucao("L1", "E1");
        assertEquals("DEVOLVIDO", estado("L1"));

        assertThrows(TransicaoInvalidaException.class, () -> reservar("L1", "E2"));

        assertEquals("DEVOLVIDO", estado("L1"));
        assertFalse(sistema.movimentacao().reservaAtiva("L1").isPresent(), "nenhuma reserva pode ter sido criada");
    }

    @Test
    @DisplayName("R01: laudo irrecuperavel leva REPARANDO a INCINERADO (estado final)")
    void R01_laudoIrrecuperavelLevaReparandoAIncinerado() {
        reservarEEmprestar("L2", "E1");
        sistema.tratadorDeDevolucao().registrarDevolucao("L2", "E1");
        sistema.tratadorDeDevolucao().registrarInspecao("L2", false);
        assertEquals("REPARANDO", estado("L2"));

        sistema.tratadorDeDevolucao().registrarLaudoIrrecuperavel("L2");

        assertEquals("INCINERADO", estado("L2"));
        assertThrows(TransicaoInvalidaException.class, () -> reservar("L2", "E1"));
        assertThrows(TransicaoInvalidaException.class, () -> sistema.tratadorDeDevolucao().concluirReparo("L2"));
    }

    @Test
    @DisplayName("R01: evento recusado nao altera o livro (teste direto no dominio)")
    void R01_eventoInvalidoNaoAlteraOLivro() {
        Livro livro = Livro.novo("X1", "Titulo", "Autor");
        Emprestimo emp = Emprestimo.novo("EMP-X", "X1", "E1", HOJE);

        assertThrows(TransicaoInvalidaException.class, () -> livro.renovar(emp));   // LIVRE nao aceita renovar
        assertThrows(TransicaoInvalidaException.class, () -> livro.devolver(HOJE));
        assertThrows(TransicaoInvalidaException.class, livro::emprestar);

        assertEquals("LIVRE", livro.getNomeEstado());
        assertEquals(0, livro.getNumRenovacoes());
        assertEquals(HOJE.plusDays(14), emp.getDataVencimento());
    }

    @Test
    @DisplayName("R01: todas as transicoes do diagrama de estados")
    void R01_transicoesDoDiagramaDeEstados() {
        Livro l = Livro.novo("X2", "T", "A");
        Emprestimo e = Emprestimo.novo("EMP", "X2", "E1", HOJE);
        l.enviarParaReparo();          assertEquals("REPARANDO", l.getNomeEstado());
        l.concluirReparo();            assertEquals("LIVRE", l.getNomeEstado());
        l.reservar();                  assertEquals("RESERVADO", l.getNomeEstado());
        l.emprestar();                 assertEquals("EMPRESTADO", l.getNomeEstado());
        l.devolver(HOJE);              assertEquals("DEVOLVIDO", l.getNomeEstado());
        assertEquals(HOJE, l.getDataUltimaDevolucao(), "entry / registrarDataDevolucao");
        l.aprovarInspecao();           assertEquals("LIVRE", l.getNomeEstado());
        l.reservar();
        l.emprestar();
        l.renovar(e);                  assertEquals("RENOVADO", l.getNomeEstado());
        l.renovar(e);                  assertEquals("RENOVADO", l.getNomeEstado());
        l.devolver(HOJE);              assertEquals("DEVOLVIDO", l.getNomeEstado());
        l.reprovarInspecao();          assertEquals("REPARANDO", l.getNomeEstado());
        l.registrarLaudoIrrecuperavel(); assertEquals("INCINERADO", l.getNomeEstado());
    }

    @Test
    @DisplayName("R01: estado e gravado pelo nome e reconstituido na leitura")
    void R01_estadoGravadoEReconstituidoNaPersistencia() {
        reservarEEmprestar("L3", "E1");
        renovar("L3", "E1");

        Livro relido = sistema.fabrica().repositorioLivro().buscar("L3").orElseThrow();

        assertEquals("RENOVADO", relido.getNomeEstado());
        assertEquals(1, relido.getNumRenovacoes());
        relido.renovar(sistema.movimentacao().consultarEmprestimo("L3", "E1"));   // o estado reconstituido funciona
        assertEquals(2, relido.getNumRenovacoes());
    }
}
