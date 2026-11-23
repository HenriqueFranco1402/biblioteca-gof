package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.banco.BancoBApi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * PADRAO ADAPTER - papel: Adaptador do Banco B (adaptador de objeto: compoe a {@link BancoBApi}).
 *
 * <p>Conversoes feitas AQUI e somente aqui:</p>
 * <ul>
 *   <li>valor: {@code Dinheiro} -> BigDecimal em reais com 2 casas. R$ 7,00 -> 7.00;</li>
 *   <li>documento: "12345678909" -> "123.456.789-09";</li>
 *   <li>identificador: id institucional vai como referenciaExterna; UUID do banco <-> id institucional;</li>
 *   <li>situacao: "PAID" -> PAGO, "CANCELED" -> CANCELADO, outros -> OUTRA.</li>
 * </ul>
 */
public class AdaptadorBancoB implements GatewayCobranca, SimuladorDePagamento {

    private final BancoBApi api;
    private final Map<String, String> boletoIdParaIdCobranca = new ConcurrentHashMap<>();
    private final List<Consumer<NotificacaoPagamento>> receptores = new CopyOnWriteArrayList<>();

    public AdaptadorBancoB(BancoBApi api) {
        this.api = api;
        api.assinarEventos(this::traduzirEvento);
    }

    @Override
    public RegistroCobranca registrar(SolicitacaoCobranca s) {
        BancoBApi.BoletoEmitido emitido = api.emitirBoleto(new BancoBApi.PedidoBoleto(
                s.valor().emReais(),
                formatarCpf(s.cpfPagador()),
                s.idCobranca(),
                s.vencimento()));
        boletoIdParaIdCobranca.put(emitido.boletoId(), s.idCobranca());
        return new RegistroCobranca(s.idCobranca(), "B-" + emitido.boletoId(), emitido.codigoDeBarras());
    }

    @Override
    public void aoReceberPagamento(Consumer<NotificacaoPagamento> receptor) {
        receptores.add(receptor);
    }

    @Override
    public String banco() {
        return "B";
    }

    /** Traduz o evento do Banco B para o formato institucional. */
    void traduzirEvento(BancoBApi.EventoPagamento evento) {
        String idCobranca = boletoIdParaIdCobranca.get(evento.boletoId());
        if (idCobranca == null) {
            return;
        }
        NotificacaoPagamento.SituacaoPagamento situacao = switch (evento.status()) {
            case "PAID" -> NotificacaoPagamento.SituacaoPagamento.PAGO;
            case "CANCELED" -> NotificacaoPagamento.SituacaoPagamento.CANCELADO;
            default -> NotificacaoPagamento.SituacaoPagamento.OUTRA;
        };
        NotificacaoPagamento n = new NotificacaoPagamento(idCobranca, situacao, evento.pagoEm());
        receptores.forEach(r -> r.accept(n));
    }

    static String formatarCpf(String digitos) {
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    /** Usado pela demonstracao: o "banco" paga o boleto de uma cobranca. */
    @Override
    public void simularPagamento(String idCobranca, java.time.LocalDate data) {
        boletoIdParaIdCobranca.forEach((boletoId, id) -> {
            if (id.equals(idCobranca)) {
                api.simularPagamento(boletoId, data);
            }
        });
    }
}
