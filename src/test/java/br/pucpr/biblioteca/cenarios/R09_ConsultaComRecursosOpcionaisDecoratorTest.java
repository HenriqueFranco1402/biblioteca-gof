package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.consulta.ArmazenamentoCache;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.ConsultaAcervoOriginal;
import br.pucpr.biblioteca.consulta.ConsultaComCache;
import br.pucpr.biblioteca.consulta.ConsultaComMedicao;
import br.pucpr.biblioteca.consulta.MetadadosLivro;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 9 - Servico de consulta com recursos opcionais (Decorator). */
@DisplayName("R09 - Decorator: consulta com cache e medicao")
class R09_ConsultaComRecursosOpcionaisDecoratorTest extends CenarioBase {

    /** Conta quantas consultas chegam de fato ao acervo. */
    private static final class Contador implements ConsultaAcervo {
        final ConsultaAcervo alvo;
        final AtomicInteger chamadas = new AtomicInteger();

        Contador(ConsultaAcervo alvo) { this.alvo = alvo; }

        public Optional<MetadadosLivro> consultarMetadados(String id) {
            chamadas.incrementAndGet();
            return alvo.consultarMetadados(id);
        }
    }

    @Test
    @DisplayName("R09 cenario: a segunda consulta de metadados usa o cache e ainda assim gera medicao")
    void R09_segundaConsultaUsaCacheEGeraMedicao() {
        Contador acervoReal = new Contador(new ConsultaAcervoOriginal(sistema.acervo()));
        ConsultaComCache cache = new ConsultaComCache(acervoReal, ArmazenamentoCache.emMemoria());
        ConsultaComMedicao medicao = new ConsultaComMedicao(cache);        // medicao( cache( original ) )

        MetadadosLivro primeira = medicao.consultarMetadados("L1").orElseThrow();
        MetadadosLivro segunda = medicao.consultarMetadados("L1").orElseThrow();

        assertEquals(primeira, segunda);
        assertEquals(1, acervoReal.chamadas.get(), "a segunda veio do cache");
        assertEquals(1, cache.acertos());
        assertEquals(2, medicao.totalDeConsultas(), "as duas foram medidas");
    }

    @Test
    @DisplayName("R09 cenario: resultado antigo do cache nunca autoriza reservar livro que deixou de estar LIVRE")
    void R09_cacheAntigoNaoAutorizaReserva() {
        // consulta enche o cache enquanto L1 esta LIVRE
        assertEquals("LIVRE", sistema.tratadorDeAcervo().consultarLivro("L1").orElseThrow().estado());
        reservar("L1", "E1");                                 // L1 -> RESERVADO

        // o cache continua com os metadados, mas a ficha mostra o estado atual
        assertTrue(sistema.consultaAcervo().consultarMetadados("L1").isPresent());
        assertEquals("RESERVADO", sistema.tratadorDeAcervo().consultarLivro("L1").orElseThrow().estado());

        // a reserva consulta o Livro atual (State), nao o cache
        assertThrows(TransicaoInvalidaException.class, () -> reservar("L1", "E2"));
    }

    @Test
    @DisplayName("R09: se o cache falhar, a consulta degrada para o acervo e nao quebra")
    void R09_falhaDoCacheDegradaParaOriginal() {
        ArmazenamentoCache quebrado = new ArmazenamentoCache() {
            public Optional<MetadadosLivro> obter(String c) { throw new IllegalStateException("cache fora do ar"); }

            public void guardar(String c, MetadadosLivro v) { throw new IllegalStateException("cache fora do ar"); }

            public void invalidar(String c) { }
        };
        ConsultaComCache cache = new ConsultaComCache(new ConsultaAcervoOriginal(sistema.acervo()), quebrado);

        assertEquals("Padroes de Projeto", cache.consultarMetadados("L1").orElseThrow().titulo());
        assertEquals(1, cache.falhasDeCache());
    }

    @Test
    @DisplayName("R09: a ordem importa - cache(medicao(original)) so mede as consultas que chegam ao acervo")
    void R09_ordemDosDecoradoresImporta() {
        ConsultaComMedicao medicaoInterna = new ConsultaComMedicao(new ConsultaAcervoOriginal(sistema.acervo()));
        ConsultaComCache cacheExterno = new ConsultaComCache(medicaoInterna, ArmazenamentoCache.emMemoria());

        cacheExterno.consultarMetadados("L2");
        cacheExterno.consultarMetadados("L2");

        assertEquals(1, medicaoInterna.totalDeConsultas());
    }
}
