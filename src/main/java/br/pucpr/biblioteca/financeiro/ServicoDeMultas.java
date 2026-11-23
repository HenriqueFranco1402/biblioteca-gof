package br.pucpr.biblioteca.financeiro;

import br.pucpr.biblioteca.financeiro.cobranca.GatewayCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.NotificacaoPagamento;
import br.pucpr.biblioteca.financeiro.politica.ConfiguracaoAdministrativa;
import br.pucpr.biblioteca.financeiro.politica.PoliticaMulta;
import br.pucpr.biblioteca.notificacao.ServicoEmail;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servico de multas do componente Financeiro (interfaces iMulta e iQuitacao).
 *
 * <ul>
 *   <li>PADRAO STRATEGY - papel: Contexto. Usa a {@link PoliticaMulta} vigente sem saber qual e.</li>
 *   <li>PADRAO ADAPTER - papel: Cliente. Fala so com {@link GatewayCobranca}; nao existe nenhum
 *       {@code if} por banco nesta classe.</li>
 * </ul>
 */
public class ServicoDeMultas {

    private final ConfiguracaoAdministrativa configuracao;
    private final GatewayCobranca gateway;
    private final ServicoEmail email;
    private final Clock relogio;
    private final Map<String, Multa> multas = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger();
    private final AtomicInteger quitacoesEfetivas = new AtomicInteger();

    public ServicoDeMultas(ConfiguracaoAdministrativa configuracao, GatewayCobranca gateway,
                           ServicoEmail email, Clock relogio) {
        this.configuracao = configuracao;
        this.gateway = gateway;
        this.email = email;
        this.relogio = relogio;
        // As notificacoes de pagamento chegam do banco ja traduzidas pelo adaptador.
        gateway.aoReceberPagamento(this::registrarPagamento);
    }

    /** Dias corridos de atraso: data da devolucao - vencimento (nunca negativo). */
    public static long diasDeAtraso(LocalDate vencimento, LocalDate dataDevolucao) {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, dataDevolucao));
    }

    /**
     * Emite a multa de uma devolucao. Se a politica vigente resultar em zero, nao ha multa.
     * O valor e o nome da politica sao congelados na Multa.
     */
    public Optional<Multa> emitirMulta(SolicitacaoDeMulta s) {
        long dias = diasDeAtraso(s.vencimento(), s.dataDevolucao());
        PoliticaMulta politica = configuracao.politicaVigente();
        Dinheiro valor = politica.calcular(dias);
        if (valor.ehZero()) {
            return Optional.empty();
        }
        LocalDate hoje = LocalDate.now(relogio);
        Multa multa = new Multa("MUL-" + sequencia.incrementAndGet(), s.idDevolucao(), s.idEstudante(),
                dias, valor, politica.nome(), hoje);
        multas.put(multa.getIdMulta(), multa);

        // Emissao do boleto: identica para qualquer politica e qualquer banco.
        Boleto boleto = Boleto.criar(multa, s.cpfEstudante(), hoje, gateway);
        try {
            boleto.enviarPorEmail(s.emailEstudante(), email);
        } catch (RuntimeException falhaEmail) {
            // o boleto continua registrado no banco; o e-mail pode ser reenviado depois
        }
        return Optional.of(multa);
    }

    /**
     * iQuitacao: recebe a notificacao (ja traduzida pelo adaptador) e quita a multa.
     * IDEMPOTENTE: uma notificacao repetida encontra a multa ja QUITADA e e ignorada.
     *
     * @return true somente na primeira quitacao
     */
    public boolean registrarPagamento(NotificacaoPagamento notificacao) {
        if (!notificacao.pago()) {
            return false;
        }
        Multa multa = multas.get(notificacao.idCobranca());
        if (multa == null) {
            return false;
        }
        boolean quitou = multa.quitar(notificacao.dataPagamento());
        if (quitou) {
            quitacoesEfetivas.incrementAndGet();
        }
        return quitou;
    }

    public Optional<Multa> multa(String idMulta) {
        return Optional.ofNullable(multas.get(idMulta));
    }

    public List<Multa> multasDoEstudante(String idEstudante) {
        return multas.values().stream().filter(m -> m.getIdEstudante().equals(idEstudante))
                .sorted((a, b) -> a.getIdMulta().compareTo(b.getIdMulta())).toList();
    }

    public List<Multa> todas() {
        return multas.values().stream().sorted((a, b) -> a.getIdMulta().compareTo(b.getIdMulta())).toList();
    }

    public int quitacoesEfetivas() {
        return quitacoesEfetivas.get();
    }
}
