package br.pucpr.biblioteca.app;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Relogio que pode ser avancado. Permite demonstrar e testar uma devolucao atrasada sem esperar
 * dias reais (comando "avancar N" da interface).
 */
public class RelogioAjustavel extends Clock {

    private volatile Instant agora;
    private final ZoneId zona;

    public RelogioAjustavel(Instant inicio, ZoneId zona) {
        this.agora = inicio;
        this.zona = zona;
    }

    public static RelogioAjustavel em(LocalDate data) {
        return new RelogioAjustavel(data.atStartOfDay(ZoneOffset.UTC).toInstant().plus(Duration.ofHours(12)), ZoneOffset.UTC);
    }

    public static RelogioAjustavel agora() {
        return em(LocalDate.now());
    }

    public void avancarDias(long dias) {
        agora = agora.plus(Duration.ofDays(dias));
    }

    public LocalDate hoje() {
        return LocalDate.ofInstant(agora, zona);
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId novaZona) {
        return new RelogioAjustavel(agora, novaZona);
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
