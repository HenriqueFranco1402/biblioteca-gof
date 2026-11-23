package br.pucpr.biblioteca.circulacao.solicitacao;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * PADRAO COMMAND - papel: Comando (abstrato).
 *
 * <p>Uma solicitacao de circulacao vira um OBJETO com identificador, autor, instante de criacao e
 * a operacao {@link #executar(FachadaCirculacao)}. Quem cria e a tela (Cliente); quem invoca e o
 * {@link ExecutorDeSolicitacoes} (Invocador); quem executa de fato e a fachada/Livro (Receptor).
 * As regras continuam no dominio: o comando so chama a fachada.</p>
 *
 * <p>Nao ha "desfazer": uma reserva ou renovacao ja efetivada e um fato de negocio (pode ter gerado
 * prazo, e-mail, etc.). Corrigir e outra operacao de dominio, nao um undo tecnico.</p>
 */
public abstract class SolicitacaoCirculacao {

    private final String id;
    private final String autor;
    private final Instant criadaEm;

    protected SolicitacaoCirculacao(String id, String autor, Instant criadaEm) {
        this.id = Objects.requireNonNull(id, "id da solicitacao");
        this.autor = Objects.requireNonNull(autor, "autor da solicitacao");
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    /** Gera um identificador novo (a tela gera UM por submissao do formulario). */
    public static String novoId() {
        return UUID.randomUUID().toString();
    }

    public abstract Comprovante executar(FachadaCirculacao receptor);

    public abstract String descricao();

    public String getId() { return id; }

    public String getAutor() { return autor; }

    public Instant getCriadaEm() { return criadaEm; }
}
