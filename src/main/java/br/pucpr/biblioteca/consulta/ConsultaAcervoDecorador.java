package br.pucpr.biblioteca.consulta;

import java.util.Objects;
import java.util.Optional;

/** PADRAO DECORATOR - papel: Decorador abstrato. Guarda o componente embrulhado e, por padrao, so repassa. */
public abstract class ConsultaAcervoDecorador implements ConsultaAcervo {

    protected final ConsultaAcervo embrulhado;

    protected ConsultaAcervoDecorador(ConsultaAcervo embrulhado) {
        this.embrulhado = Objects.requireNonNull(embrulhado);
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        return embrulhado.consultarMetadados(idLivro);
    }
}
