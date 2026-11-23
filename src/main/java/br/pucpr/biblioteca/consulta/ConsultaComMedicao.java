package br.pucpr.biblioteca.consulta;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PADRAO DECORATOR - Decorador concreto de MEDICAO: conta as consultas e o tempo gasto.
 *
 * <p>A ordem importa: em medicao(cache(original)) toda consulta e medida, inclusive as atendidas
 * pelo cache; em cache(medicao(original)) so as consultas que chegam ao acervo seriam medidas.</p>
 */
public class ConsultaComMedicao extends ConsultaAcervoDecorador {

    private final AtomicLong consultas = new AtomicLong();
    private final AtomicLong nanosAcumulados = new AtomicLong();

    public ConsultaComMedicao(ConsultaAcervo embrulhado) {
        super(embrulhado);
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        long inicio = System.nanoTime();
        try {
            return super.consultarMetadados(idLivro);
        } finally {
            consultas.incrementAndGet();
            nanosAcumulados.addAndGet(System.nanoTime() - inicio);
        }
    }

    public long totalDeConsultas() {
        return consultas.get();
    }

    public long microssegundosAcumulados() {
        return nanosAcumulados.get() / 1_000;
    }
}
