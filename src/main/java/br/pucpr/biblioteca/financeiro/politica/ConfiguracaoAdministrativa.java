package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuracao administrativa que define a politica de multa VIGENTE (Strategy).
 *
 * <p>As politicas disponiveis ficam num registro por nome. Uma nova politica e incluida com
 * {@link #registrar(PoliticaMulta)} na montagem do sistema, sem alterar nenhuma classe existente.</p>
 */
public class ConfiguracaoAdministrativa {

    private final Map<String, PoliticaMulta> disponiveis = new LinkedHashMap<>();
    private volatile PoliticaMulta vigente;

    public ConfiguracaoAdministrativa() {
        registrar(new PoliticaOriginal());
        registrar(new PoliticaComTeto());
        registrar(new PoliticaComTolerancia());
        this.vigente = disponiveis.get(PoliticaOriginal.NOME);
    }

    public final void registrar(PoliticaMulta politica) {
        disponiveis.put(politica.nome().toUpperCase(), politica);
    }

    public void definirPoliticaVigente(String nome) {
        PoliticaMulta politica = disponiveis.get(nome.toUpperCase());
        if (politica == null) {
            throw new RegraDeNegocioException("Politica de multa desconhecida: " + nome
                    + ". Disponiveis: " + disponiveis.keySet());
        }
        this.vigente = politica;
    }

    public PoliticaMulta politicaVigente() {
        return vigente;
    }

    public List<String> politicasDisponiveis() {
        return List.copyOf(disponiveis.keySet());
    }
}
