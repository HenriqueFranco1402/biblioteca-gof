package br.pucpr.biblioteca.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetario em reais, guardado em CENTAVOS (long) para evitar erros de ponto flutuante.
 * R$ 7,00 = 700 centavos. As conversoes para cada banco ficam nos adaptadores.
 */
public record Dinheiro(long centavos) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(0);

    public Dinheiro {
        if (centavos < 0) {
            throw new IllegalArgumentException("Valor monetario negativo");
        }
    }

    public static Dinheiro reais(long reais) {
        return new Dinheiro(Math.multiplyExact(reais, 100));
    }

    public static Dinheiro reais(String valor) {
        return new Dinheiro(new BigDecimal(valor).setScale(2, RoundingMode.HALF_EVEN).movePointRight(2).longValueExact());
    }

    public Dinheiro vezes(long quantidade) {
        return new Dinheiro(Math.multiplyExact(centavos, quantidade));
    }

    public Dinheiro limitadoA(Dinheiro teto) {
        return compareTo(teto) > 0 ? teto : this;
    }

    public boolean ehZero() {
        return centavos == 0;
    }

    /** Valor em reais com duas casas: 700 centavos -> 7.00 */
    public BigDecimal emReais() {
        return BigDecimal.valueOf(centavos, 2);
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return Long.compare(centavos, outro.centavos);
    }

    @Override
    public String toString() {
        return "R$ " + emReais().toPlainString().replace('.', ',');
    }
}
