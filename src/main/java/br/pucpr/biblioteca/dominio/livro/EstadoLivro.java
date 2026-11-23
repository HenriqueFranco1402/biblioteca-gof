package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;

import java.time.LocalDate;
import java.util.Map;

/**
 * PADRAO STATE - papel: Estado abstrato.
 *
 * <p>Declara um metodo por evento da maquina de estados do Livro. A implementacao padrao de
 * cada evento RECUSA a operacao lancando {@link TransicaoInvalidaException} <b>antes</b> de tocar
 * no livro - por isso um evento invalido nunca altera o objeto. Cada estado concreto
 * sobrescreve somente as transicoes que saem dele no diagrama de estados.</p>
 *
 * <pre>
 * Transicoes (estado de origem -- evento [guarda] / acao --&gt; destino):
 *   LIVRE      -- reservar                                        --&gt; RESERVADO
 *   LIVRE      -- enviarParaReparo                                --&gt; REPARANDO
 *   RESERVADO  -- emprestar / numRenovacoes = 0                   --&gt; EMPRESTADO
 *   EMPRESTADO -- renovar [numRenovacoes &lt; 2] / vencimento += 30  --&gt; RENOVADO
 *   EMPRESTADO -- devolver                                        --&gt; DEVOLVIDO
 *   RENOVADO   -- renovar [numRenovacoes &lt; 2] / vencimento += 30  --&gt; RENOVADO
 *   RENOVADO   -- devolver                                        --&gt; DEVOLVIDO
 *   DEVOLVIDO  (entry / registrarDataDevolucao)
 *   DEVOLVIDO  -- aprovarInspecao  (bom estado)                   --&gt; LIVRE
 *   DEVOLVIDO  -- reprovarInspecao (danificado)                   --&gt; REPARANDO
 *   REPARANDO  (do / reparar)
 *   REPARANDO  -- concluirReparo                                  --&gt; LIVRE
 *   REPARANDO  -- registrarLaudoIrrecuperavel                     --&gt; INCINERADO (final)
 * </pre>
 */
public abstract class EstadoLivro {

    /**
     * Registro nome -> estado. Fica numa classe interna ("holder") para ser inicializado so no
     * primeiro uso, depois que as subclasses ja existem (evita ciclo de inicializacao estatica).
     */
    private static final class Registro {
        static final Map<String, EstadoLivro> POR_NOME = Map.of(
                EstadoLivre.INSTANCIA.nome(), EstadoLivre.INSTANCIA,
                EstadoReservado.INSTANCIA.nome(), EstadoReservado.INSTANCIA,
                EstadoEmprestado.INSTANCIA.nome(), EstadoEmprestado.INSTANCIA,
                EstadoRenovado.INSTANCIA.nome(), EstadoRenovado.INSTANCIA,
                EstadoDevolvido.INSTANCIA.nome(), EstadoDevolvido.INSTANCIA,
                EstadoReparando.INSTANCIA.nome(), EstadoReparando.INSTANCIA,
                EstadoIncinerado.INSTANCIA.nome(), EstadoIncinerado.INSTANCIA);
    }

    /** Reconstitui o objeto de estado a partir do nome gravado na persistencia. */
    public static EstadoLivro deNome(String nome) {
        EstadoLivro estado = Registro.POR_NOME.get(nome);
        if (estado == null) {
            throw new IllegalArgumentException("Estado de livro desconhecido na persistencia: " + nome);
        }
        return estado;
    }

    /** Nome gravado na persistencia e exibido na interface. */
    public abstract String nome();

    /** Acao de entrada (entry /). Por padrao nada acontece. */
    void aoEntrar(Livro livro) {
    }

    // ---------- eventos: por padrao, recusados sem alterar o livro ----------

    void reservar(Livro livro) { recusar(livro, "reservar"); }

    void emprestar(Livro livro) { recusar(livro, "emprestar"); }

    void renovar(Livro livro, Emprestimo emprestimo) { recusar(livro, "renovar"); }

    void devolver(Livro livro, LocalDate dataDevolucao) { recusar(livro, "devolver"); }

    void aprovarInspecao(Livro livro) { recusar(livro, "aprovar inspecao"); }

    void reprovarInspecao(Livro livro) { recusar(livro, "reprovar inspecao"); }

    void enviarParaReparo(Livro livro) { recusar(livro, "enviar para reparo"); }

    void concluirReparo(Livro livro) { recusar(livro, "concluir reparo"); }

    void registrarLaudoIrrecuperavel(Livro livro) { recusar(livro, "registrar laudo irrecuperavel"); }

    protected final void recusar(Livro livro, String evento) {
        throw new TransicaoInvalidaException(
                "Operacao '" + evento + "' nao permitida: livro " + livro.getIdLivro() + " esta " + nome());
    }

    @Override
    public String toString() {
        return nome();
    }
}
