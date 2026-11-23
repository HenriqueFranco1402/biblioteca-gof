package br.pucpr.biblioteca.consulta;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Armazenamento usado pelo decorador de cache (em producao poderia ser Redis/Memcached e falhar). */
public interface ArmazenamentoCache {

    Optional<MetadadosLivro> obter(String chave);

    void guardar(String chave, MetadadosLivro valor);

    void invalidar(String chave);

    /** Implementacao padrao em memoria. */
    static ArmazenamentoCache emMemoria() {
        Map<String, MetadadosLivro> mapa = new ConcurrentHashMap<>();
        return new ArmazenamentoCache() {
            public Optional<MetadadosLivro> obter(String chave) { return Optional.ofNullable(mapa.get(chave)); }

            public void guardar(String chave, MetadadosLivro valor) { mapa.put(chave, valor); }

            public void invalidar(String chave) { mapa.remove(chave); }
        };
    }
}
