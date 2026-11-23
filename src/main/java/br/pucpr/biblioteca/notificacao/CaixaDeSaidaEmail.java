package br.pucpr.biblioteca.notificacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementacao simulada: guarda os e-mails "enviados" numa caixa de saida em memoria. */
public class CaixaDeSaidaEmail implements ServicoEmail {

    public record Email(String para, String assunto, String corpo) {
    }

    private final List<Email> enviados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void enviar(String para, String assunto, String corpo) {
        if (para == null || para.isBlank()) {
            throw new IllegalArgumentException("Destinatario de e-mail vazio");
        }
        enviados.add(new Email(para, assunto, corpo));
    }

    public List<Email> enviados() {
        return List.copyOf(enviados);
    }
}
