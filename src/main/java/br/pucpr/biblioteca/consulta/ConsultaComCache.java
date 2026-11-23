package br.pucpr.biblioteca.consulta;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PADRAO DECORATOR - Decorador concreto de CACHE.
 *
 * <ul>
 *   <li>Armazena SOMENTE {@link MetadadosLivro} (sem estado). A reserva nunca usa esta consulta:
 *       a fachada le o Livro atual no Acervo e o State decide. Por isso um resultado antigo do cache
 *       jamais autoriza reservar um livro que deixou de estar LIVRE.</li>
 *   <li>Se o armazenamento do cache falhar (excecao), o decorador ignora o cache e consulta o
 *       componente embrulhado: o cache e uma otimizacao, nunca um ponto de falha.</li>
 * </ul>
 */
public class ConsultaComCache extends ConsultaAcervoDecorador {

    private final ArmazenamentoCache cache;
    private final AtomicLong acertos = new AtomicLong();
    private final AtomicLong falhasDeCache = new AtomicLong();

    public ConsultaComCache(ConsultaAcervo embrulhado, ArmazenamentoCache cache) {
        super(embrulhado);
        this.cache = cache;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        try {
            Optional<MetadadosLivro> emCache = cache.obter(idLivro);
            if (emCache.isPresent()) {
                acertos.incrementAndGet();
                return emCache;
            }
        } catch (RuntimeException falha) {
            falhasDeCache.incrementAndGet();
            return super.consultarMetadados(idLivro);   // degrada para a consulta original
        }
        Optional<MetadadosLivro> resultado = super.consultarMetadados(idLivro);
        resultado.ifPresent(m -> {
            try {
                cache.guardar(idLivro, m);
            } catch (RuntimeException falha) {
                falhasDeCache.incrementAndGet();
            }
        });
        return resultado;
    }

    public long acertos() {
        return acertos.get();
    }

    public long falhasDeCache() {
        return falhasDeCache.get();
    }
}
