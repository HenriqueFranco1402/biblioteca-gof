package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoRenovacao;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Requisito 6 - Solicitacoes de circulacao como objetos (Command). */
@DisplayName("R06 - Command: solicitacoes de circulacao")
class R06_SolicitacoesComoObjetosCommandTest extends CenarioBase {

    @Test
    @DisplayName("R06 cenario: renovacao executada duas vezes com o mesmo id acrescenta apenas 30 dias")
    void R06_mesmaRenovacaoDuasVezesAcrescentaApenas30Dias() {
        reservarEEmprestar("L1", "E1");
        SolicitacaoRenovacao s = novaRenovacao("L1", "E1");

        Comprovante primeira = sistema.tratadorDeEmprestimo().submeter(s);
        Comprovante segunda = sistema.tratadorDeEmprestimo().submeter(s);   // reenvio do mesmo formulario

        assertSame(primeira, segunda, "a repeticao recebe o resultado da primeira execucao");
        assertEquals(HOJE.plusDays(14 + 30), vencimento("L1"));
        assertEquals(1, sistema.acervo().consultarLivro("L1").getNumRenovacoes());
    }

    @Test
    @DisplayName("R06 cenario: solicitacoes diferentes continuam sujeitas ao limite de duas renovacoes")
    void R06_solicitacoesDiferentesRespeitamLimiteDeDuas() {
        reservarEEmprestar("L1", "E1");
        renovar("L1", "E1");
        renovar("L1", "E1");

        assertThrows(TransicaoInvalidaException.class, () -> renovar("L1", "E1"));
        assertEquals(HOJE.plusDays(14 + 60), vencimento("L1"));
    }

    @Test
    @DisplayName("R06: duas submissoes simultaneas com o mesmo id executam uma unica vez")
    void R06_submissoesSimultaneasExecutamUmaVez() throws Exception {
        reservarEEmprestar("L1", "E1");
        SolicitacaoRenovacao s = novaRenovacao("L1", "E1");
        int threads = 8;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Comprovante>> resultados = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            resultados.add(pool.submit(() -> {
                largada.await();
                return sistema.tratadorDeEmprestimo().submeter(s);
            }));
        }
        largada.countDown();
        Comprovante primeiro = resultados.get(0).get();
        for (Future<Comprovante> f : resultados) {
            assertSame(primeiro, f.get());
        }
        pool.shutdown();

        assertEquals(HOJE.plusDays(14 + 30), vencimento("L1"));
        assertEquals(1, sistema.acervo().consultarLivro("L1").getNumRenovacoes());
    }
}
