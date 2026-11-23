package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.notificacao.CaixaDeSaidaEmail;
import br.pucpr.biblioteca.notificacao.ServicoEmail;

import java.util.List;

/** Expoe a interface a caixa de saida de e-mails simulada (somente leitura). */
public class TratadorDeNotificacoes {

    private final ServicoEmail email;

    public TratadorDeNotificacoes(ServicoEmail email) {
        this.email = email;
    }

    public List<String> emailsEnviados() {
        if (email instanceof CaixaDeSaidaEmail caixa) {
            return caixa.enviados().stream().map(e -> "para " + e.para() + " | " + e.assunto() + " | " + e.corpo()).toList();
        }
        return List.of("(servico de e-mail real: caixa de saida nao disponivel)");
    }
}
