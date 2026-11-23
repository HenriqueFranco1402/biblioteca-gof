package br.pucpr.biblioteca.consulta;

import java.util.Optional;

/**
 * PADRAO DECORATOR - papel: Componente (contrato de consulta ao acervo).
 * A implementacao original e os decoradores implementam o mesmo contrato, por isso podem ser
 * empilhados em qualquer combinacao.
 */
public interface ConsultaAcervo {

    Optional<MetadadosLivro> consultarMetadados(String idLivro);
}
