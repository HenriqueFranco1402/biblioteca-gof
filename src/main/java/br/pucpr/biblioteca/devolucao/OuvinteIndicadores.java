package br.pucpr.biblioteca.devolucao;

/** PADRAO OBSERVER - Observador concreto: alimenta os indicadores de devolucao/atraso. */
public class OuvinteIndicadores implements OuvinteDevolucao {

    private final Indicadores indicadores;

    public OuvinteIndicadores(Indicadores indicadores) {
        this.indicadores = indicadores;
    }

    @Override
    public String nome() {
        return "indicadores";
    }

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        indicadores.registrar(evento);
    }
}
