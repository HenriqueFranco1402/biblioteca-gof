package br.pucpr.biblioteca.consulta;

import br.pucpr.biblioteca.acesso.Acervo;

import java.util.Optional;

/** PADRAO DECORATOR - papel: Componente concreto. A consulta original, sem alteracoes: le o Acervo. */
public class ConsultaAcervoOriginal implements ConsultaAcervo {

    private final Acervo acervo;

    public ConsultaAcervoOriginal(Acervo acervo) {
        this.acervo = acervo;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(String idLivro) {
        return acervo.buscar(idLivro).map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo(), l.getAutor()));
    }
}
