package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.banco.BancoAApi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * PADRAO ADAPTER - papel: Adaptador do Banco A (adaptador de objeto: compoe a {@link BancoAApi}).
 *
 * <p>Conversoes feitas AQUI e somente aqui:</p>
 * <ul>
 *   <li>valor: {@code Dinheiro} -> centavos (long). R$ 7,00 -> 700;</li>
 *   <li>documento: CPF "12345678909" -> numero long;</li>
 *   <li>identificador: id institucional -> "seuNumero"; protocolo do banco <-> id institucional;</li>
 *   <li>data: LocalDate -> inteiro AAAAMMDD;</li>
 *   <li>situacao: codigo "06" -> PAGO, "09" -> CANCELADO, outros -> OUTRA.</li>
 * </ul>
 */
public class AdaptadorBancoA implements GatewayCobranca, SimuladorDePagamento {

    private static final DateTimeFormatter AAAAMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final BancoAApi api;
    private final Map<Integer, String> protocoloParaIdCobranca = new ConcurrentHashMap<>();
    private final List<Consumer<NotificacaoPagamento>> receptores = new CopyOnWriteArrayList<>();

    public AdaptadorBancoA(BancoAApi api) {
        this.api = api;
        api.registrarOuvinteDeRetorno(this::traduzirRetorno);
    }

    @Override
    public RegistroCobranca registrar(SolicitacaoCobranca s) {
        int protocolo = api.registrarTitulo(
                s.valor().centavos(),
                Long.parseLong(s.cpfPagador()),
                s.idCobranca(),
                Integer.parseInt(s.vencimento().format(AAAAMMDD)));
        protocoloParaIdCobranca.put(protocolo, s.idCobranca());
        return new RegistroCobranca(s.idCobranca(), "A-" + protocolo, "00190.00009 " + protocolo);
    }

    @Override
    public void aoReceberPagamento(Consumer<NotificacaoPagamento> receptor) {
        receptores.add(receptor);
    }

    @Override
    public String banco() {
        return "A";
    }

    /** Traduz a linha "protocolo;codigo;AAAAMMDD" do Banco A para o formato institucional. */
    void traduzirRetorno(String linha) {
        String[] campos = linha.split(";");
        String idCobranca = protocoloParaIdCobranca.get(Integer.parseInt(campos[0]));
        if (idCobranca == null) {
            return; // titulo que nao foi registrado por esta instalacao
        }
        NotificacaoPagamento.SituacaoPagamento situacao = switch (campos[1]) {
            case BancoAApi.LIQUIDADO -> NotificacaoPagamento.SituacaoPagamento.PAGO;
            case BancoAApi.BAIXADO -> NotificacaoPagamento.SituacaoPagamento.CANCELADO;
            default -> NotificacaoPagamento.SituacaoPagamento.OUTRA;
        };
        NotificacaoPagamento n = new NotificacaoPagamento(idCobranca, situacao, LocalDate.parse(campos[2], AAAAMMDD));
        receptores.forEach(r -> r.accept(n));
    }

    /** Usado pela demonstracao: o "banco" liquida o titulo de uma cobranca. */
    @Override
    public void simularPagamento(String idCobranca, LocalDate data) {
        protocoloParaIdCobranca.forEach((protocolo, id) -> {
            if (id.equals(idCobranca)) {
                api.simularLiquidacao(protocolo, Integer.parseInt(data.format(AAAAMMDD)));
            }
        });
    }
}
