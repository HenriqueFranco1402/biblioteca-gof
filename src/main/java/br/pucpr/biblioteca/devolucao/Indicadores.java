package br.pucpr.biblioteca.devolucao;

import java.util.concurrent.atomic.AtomicLong;

/** Indicadores operacionais alimentados pelo ouvinte de indicadores. */
public class Indicadores {

    public record Resumo(long devolucoes, long devolucoesAtrasadas, long diasDeAtrasoAcumulados) {
    }

    private final AtomicLong devolucoes = new AtomicLong();
    private final AtomicLong atrasadas = new AtomicLong();
    private final AtomicLong diasDeAtraso = new AtomicLong();

    void registrar(EventoDevolucao evento) {
        devolucoes.incrementAndGet();
        if (evento.atrasada()) {
            atrasadas.incrementAndGet();
            diasDeAtraso.addAndGet(evento.diasDeAtraso());
        }
    }

    public Resumo resumo() {
        return new Resumo(devolucoes.get(), atrasadas.get(), diasDeAtraso.get());
    }
}
