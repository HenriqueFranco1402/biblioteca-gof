package br.pucpr.biblioteca.circulacao.solicitacao;

import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * PADRAO COMMAND - papel: Invocador.
 *
 * <p>Garante que o MESMO identificador nunca seja executado duas vezes, inclusive com duas
 * submissoes simultaneas: {@link ConcurrentMap#putIfAbsent} e atomico, entao so uma thread
 * consegue registrar a tarefa daquele id. Essa thread executa; as demais (e qualquer repeticao
 * posterior) apenas aguardam/recebem o MESMO resultado - ou a mesma falha.</p>
 */
public class ExecutorDeSolicitacoes {

    private final FachadaCirculacao receptor;
    private final ConcurrentMap<String, FutureTask<Comprovante>> execucoes = new ConcurrentHashMap<>();

    public ExecutorDeSolicitacoes(FachadaCirculacao receptor) {
        this.receptor = receptor;
    }

    public Comprovante executar(SolicitacaoCirculacao solicitacao) {
        FutureTask<Comprovante> nova = new FutureTask<>(() -> solicitacao.executar(receptor));
        FutureTask<Comprovante> existente = execucoes.putIfAbsent(solicitacao.getId(), nova);
        FutureTask<Comprovante> tarefa = existente == null ? nova : existente;
        if (existente == null) {
            nova.run();                 // somente quem registrou o id executa
        }
        try {
            return tarefa.get();        // repeticoes recebem o resultado da primeira execucao
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execucao interrompida", e);
        }
    }

    public boolean jaExecutada(String idSolicitacao) {
        return execucoes.containsKey(idSolicitacao);
    }

    public int totalExecutadas() {
        return execucoes.size();
    }
}
