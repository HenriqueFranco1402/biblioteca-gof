package br.pucpr.biblioteca.notificacao;

/** Envio de e-mail (infraestrutura). Pode falhar: quem chama decide como tratar a falha. */
public interface ServicoEmail {
    void enviar(String para, String assunto, String corpo);
}
