package br.pucpr.biblioteca.devolucao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PADRAO OBSERVER - papel: Sujeito / Publicador.
 *
 * <p>Mantem a lista de ouvintes e notifica cada um, ISOLADAMENTE: a excecao de um ouvinte e
 * capturada e registrada, e os demais continuam sendo notificados. A devolucao ja foi confirmada
 * (commit) antes da publicacao, entao nenhuma falha aqui consegue desfaze-la.</p>
 */
public class PublicadorDevolucao {

    public record FalhaDeOuvinte(String ouvinte, String idDevolucao, String mensagem) {
    }

    private final List<OuvinteDevolucao> ouvintes = new CopyOnWriteArrayList<>();
    private final List<FalhaDeOuvinte> falhas = Collections.synchronizedList(new ArrayList<>());

    public void registrar(OuvinteDevolucao ouvinte) {
        ouvintes.add(ouvinte);
    }

    public void remover(OuvinteDevolucao ouvinte) {
        ouvintes.remove(ouvinte);
    }

    public void publicar(EventoDevolucao evento) {
        for (OuvinteDevolucao ouvinte : ouvintes) {
            try {
                ouvinte.aoDevolver(evento);
            } catch (RuntimeException e) {
                falhas.add(new FalhaDeOuvinte(ouvinte.nome(), evento.idDevolucao(), e.getMessage()));
                System.err.println("[aviso] ouvinte '" + ouvinte.nome() + "' falhou: " + e.getMessage());
            }
        }
    }

    public List<String> nomesDosOuvintes() {
        return ouvintes.stream().map(OuvinteDevolucao::nome).toList();
    }

    public List<FalhaDeOuvinte> falhas() {
        return List.copyOf(falhas);
    }
}
