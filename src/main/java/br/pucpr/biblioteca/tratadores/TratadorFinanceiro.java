package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.devolucao.Indicadores;
import br.pucpr.biblioteca.financeiro.Multa;
import br.pucpr.biblioteca.financeiro.ServicoDeMultas;
import br.pucpr.biblioteca.financeiro.cobranca.GatewayCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.SimuladorDePagamento;
import br.pucpr.biblioteca.financeiro.politica.ConfiguracaoAdministrativa;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Tratador administrativo/financeiro: politica de multa vigente, multas, indicadores e simulacao do banco. */
public class TratadorFinanceiro {

    private final ConfiguracaoAdministrativa configuracao;
    private final ServicoDeMultas servicoDeMultas;
    private final GatewayCobranca gateway;
    private final Indicadores indicadores;
    private final Clock relogio;

    public TratadorFinanceiro(ConfiguracaoAdministrativa configuracao, ServicoDeMultas servicoDeMultas,
                              GatewayCobranca gateway, Indicadores indicadores, Clock relogio) {
        this.configuracao = configuracao;
        this.servicoDeMultas = servicoDeMultas;
        this.gateway = gateway;
        this.indicadores = indicadores;
        this.relogio = relogio;
    }

    public void definirPoliticaMulta(String nome) {
        configuracao.definirPoliticaVigente(nome);
    }

    public String politicaVigente() {
        return configuracao.politicaVigente().nome();
    }

    public List<String> politicasDisponiveis() {
        return configuracao.politicasDisponiveis();
    }

    public List<String> multasDoEstudante(String idEstudante) {
        return servicoDeMultas.multasDoEstudante(idEstudante).stream().map(Multa::toString).toList();
    }

    public List<String> todasAsMultas() {
        return servicoDeMultas.todas().stream().map(m -> m + " - estudante " + m.getIdEstudante()).toList();
    }

    public String banco() {
        return gateway.banco();
    }

    /** Demonstracao: o banco simulado paga o boleto; a notificacao volta pelo adaptador. */
    public String simularPagamentoNoBanco(String idMulta) {
        if (!(gateway instanceof SimuladorDePagamento simulador)) {
            return "Banco real: aguarde a notificacao do banco.";
        }
        int antes = servicoDeMultas.quitacoesEfetivas();
        simulador.simularPagamento(idMulta, LocalDate.now(relogio));
        boolean quitou = servicoDeMultas.quitacoesEfetivas() > antes;
        return "Banco " + gateway.banco() + " notificou o pagamento de " + idMulta + ": "
                + (quitou ? "multa QUITADA." : "notificacao ignorada (multa ja quitada ou inexistente).");
    }

    public Indicadores.Resumo indicadores() {
        return indicadores.resumo();
    }
}
