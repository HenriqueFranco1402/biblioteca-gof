package br.pucpr.biblioteca.persistencia;

import br.pucpr.biblioteca.persistencia.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.persistencia.relacional.FabricaRelacional;

/**
 * Ponto UNICO onde a familia de persistencia e escolhida a partir do perfil de execucao.
 * E chamado uma so vez, na montagem do sistema ({@code Sistema}); o resto do codigo conhece
 * apenas {@link FabricaDeDados} e as interfaces de repositorio.
 */
public final class SeletorDeFabrica {

    private SeletorDeFabrica() {
    }

    public static FabricaDeDados paraPerfil(Perfil perfil, ConfiguracaoBanco banco) {
        return switch (perfil) {
            case TESTE -> new FabricaEmMemoria();
            case PRODUCAO -> new FabricaRelacional(banco);
        };
    }
}
