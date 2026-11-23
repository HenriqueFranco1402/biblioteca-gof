package br.pucpr.biblioteca.devolucao;

import br.pucpr.biblioteca.notificacao.ServicoEmail;

/**
 * PADRAO OBSERVER - Observador concreto: envia ao estudante o comprovante da devolucao por e-mail.
 * Se o envio falhar, a excecao sobe ate o publicador, que a registra e segue para os proximos
 * ouvintes; a devolucao (ja confirmada) nao e afetada.
 */
public class OuvinteEmail implements OuvinteDevolucao {

    private final ServicoEmail email;

    public OuvinteEmail(ServicoEmail email) {
        this.email = email;
    }

    @Override
    public String nome() {
        return "email";
    }

    @Override
    public void aoDevolver(EventoDevolucao e) {
        String corpo = "Devolucao do livro " + e.tituloLivro() + " registrada em " + e.dataDevolucao()
                + (e.atrasada() ? " com " + e.diasDeAtraso() + " dia(s) de atraso." : " dentro do prazo.");
        email.enviar(e.emailEstudante(), "Comprovante de devolucao", corpo);
    }
}
