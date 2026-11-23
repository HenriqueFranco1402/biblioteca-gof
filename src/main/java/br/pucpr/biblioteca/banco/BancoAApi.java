package br.pucpr.biblioteca.banco;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * SIMULACAO da API proprietaria do BANCO A (sistema externo). Ela e INCOMPATIVEL com o contrato
 * institucional e nao deve ser alterada:
 * <ul>
 *   <li>valor em CENTAVOS (long): R$ 7,00 = 700;</li>
 *   <li>CPF do sacado como NUMERO (long), sem pontuacao;</li>
 *   <li>"seuNumero" limitado a 15 caracteres; o banco devolve um protocolo inteiro;</li>
 *   <li>vencimento como inteiro AAAAMMDD;</li>
 *   <li>retorno de pagamento como linha de texto "protocolo;codigo;AAAAMMDD", codigo "06" = liquidado.</li>
 * </ul>
 * PADRAO ADAPTER - papel: Adaptado (Adaptee).
 */
public class BancoAApi {

    public static final String LIQUIDADO = "06";
    public static final String BAIXADO = "09";

    private final AtomicInteger protocolos = new AtomicInteger(1000);
    private final Map<Integer, Long> titulos = new ConcurrentHashMap<>();
    private final List<Consumer<String>> ouvintesDeRetorno = new CopyOnWriteArrayList<>();
    private volatile long ultimoValorEmCentavos = -1;

    public int registrarTitulo(long valorEmCentavos, long cpfSacado, String seuNumero, int vencimentoAAAAMMDD) {
        if (seuNumero.length() > 15) {
            throw new IllegalArgumentException("Banco A: seuNumero excede 15 caracteres");
        }
        int protocolo = protocolos.incrementAndGet();
        titulos.put(protocolo, valorEmCentavos);
        ultimoValorEmCentavos = valorEmCentavos;
        return protocolo;
    }

    public void registrarOuvinteDeRetorno(Consumer<String> ouvinte) {
        ouvintesDeRetorno.add(ouvinte);
    }

    /** Simula o banco liquidando um titulo e enviando a linha de retorno. */
    public void simularLiquidacao(int protocolo, int dataAAAAMMDD) {
        String linha = protocolo + ";" + LIQUIDADO + ";" + dataAAAAMMDD;
        ouvintesDeRetorno.forEach(o -> o.accept(linha));
    }

    public long ultimoValorEmCentavos() {
        return ultimoValorEmCentavos;
    }
}
