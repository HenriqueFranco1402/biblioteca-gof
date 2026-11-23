package br.pucpr.biblioteca.banco;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * SIMULACAO da API proprietaria do BANCO B (sistema externo), incompativel com o Banco A e com o
 * contrato institucional:
 * <ul>
 *   <li>valor em REAIS como BigDecimal com 2 casas: R$ 7,00 = 7.00;</li>
 *   <li>documento do pagador FORMATADO "000.000.000-00";</li>
 *   <li>identificador do boleto e um UUID gerado pelo banco; a referencia externa e livre;</li>
 *   <li>eventos de pagamento como objeto {@link EventoPagamento} com status "PAID"/"CANCELED".</li>
 * </ul>
 * PADRAO ADAPTER - papel: Adaptado (Adaptee).
 */
public class BancoBApi {

    public record PedidoBoleto(BigDecimal valor, String documentoPagador, String referenciaExterna, LocalDate dataVencimento) {
    }

    public record BoletoEmitido(String boletoId, String codigoDeBarras, String status) {
    }

    public record EventoPagamento(String boletoId, String referenciaExterna, String status, LocalDate pagoEm) {
    }

    private final List<Consumer<EventoPagamento>> assinantes = new CopyOnWriteArrayList<>();
    private final java.util.Map<String, PedidoBoleto> boletos = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile BigDecimal ultimoValor;

    public BoletoEmitido emitirBoleto(PedidoBoleto pedido) {
        if (pedido.valor().scale() != 2) {
            throw new IllegalArgumentException("Banco B: valor deve ter exatamente 2 casas decimais");
        }
        if (!pedido.documentoPagador().matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            throw new IllegalArgumentException("Banco B: documento deve estar no formato 000.000.000-00");
        }
        String id = UUID.randomUUID().toString();
        boletos.put(id, pedido);
        ultimoValor = pedido.valor();
        return new BoletoEmitido(id, "34191." + Math.abs(id.hashCode()), "EMITIDO");
    }

    public void assinarEventos(Consumer<EventoPagamento> assinante) {
        assinantes.add(assinante);
    }

    public void simularPagamento(String boletoId, LocalDate data) {
        PedidoBoleto pedido = boletos.get(boletoId);
        EventoPagamento evento = new EventoPagamento(boletoId, pedido == null ? null : pedido.referenciaExterna(), "PAID", data);
        assinantes.forEach(a -> a.accept(evento));
    }

    public BigDecimal ultimoValor() {
        return ultimoValor;
    }
}
