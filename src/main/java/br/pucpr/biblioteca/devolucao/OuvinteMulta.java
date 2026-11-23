package br.pucpr.biblioteca.devolucao;

import br.pucpr.biblioteca.financeiro.ServicoDeMultas;
import br.pucpr.biblioteca.financeiro.SolicitacaoDeMulta;

/**
 * PADRAO OBSERVER - Observador concreto: pede ao Financeiro (iMulta) a emissao da multa.
 * Devolucao no prazo nao gera multa (o servico devolve vazio quando o valor e zero).
 */
public class OuvinteMulta implements OuvinteDevolucao {

    private final ServicoDeMultas servicoDeMultas;

    public OuvinteMulta(ServicoDeMultas servicoDeMultas) {
        this.servicoDeMultas = servicoDeMultas;
    }

    @Override
    public String nome() {
        return "multa";
    }

    @Override
    public void aoDevolver(EventoDevolucao e) {
        if (!e.atrasada()) {
            return;
        }
        servicoDeMultas.emitirMulta(new SolicitacaoDeMulta(e.idDevolucao(), e.idEstudante(), e.cpfEstudante(),
                e.emailEstudante(), e.vencimento(), e.dataDevolucao()));
    }
}
