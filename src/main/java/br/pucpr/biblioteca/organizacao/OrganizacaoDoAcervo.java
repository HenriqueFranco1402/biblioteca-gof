package br.pucpr.biblioteca.organizacao;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Organizacao hierarquica do acervo (Composite). Cria os grupos e mantem o INDICE
 * idLivro -> grupo, que garante que um livro esteja em no maximo um grupo em toda a organizacao
 * (inclusive entre unidades diferentes).
 */
public class OrganizacaoDoAcervo {

    private final Map<String, GrupoDoAcervo> unidades = new LinkedHashMap<>();
    private final Map<String, GrupoDoAcervo> grupoDoLivro = new LinkedHashMap<>();

    public synchronized GrupoDoAcervo criarUnidade(String nome) {
        if (unidades.containsKey(nome)) {
            throw new RegraDeNegocioException("Unidade ja existe: " + nome);
        }
        GrupoDoAcervo unidade = new GrupoDoAcervo(nome, TipoGrupo.UNIDADE, this);
        unidades.put(nome, unidade);
        return unidade;
    }

    public GrupoDoAcervo novoGrupo(String nome, TipoGrupo tipo) {
        return new GrupoDoAcervo(nome, tipo, this);
    }

    synchronized void reservarLugar(String idLivro, GrupoDoAcervo grupo) {
        GrupoDoAcervo atual = grupoDoLivro.get(idLivro);
        if (atual != null) {
            throw new RegraDeNegocioException("Livro " + idLivro + " ja pertence ao grupo '" + atual.caminho() + "'");
        }
        grupoDoLivro.put(idLivro, grupo);
    }

    /**
     * Move o livro para outro grupo. So a arvore muda: o MESMO item (mesmo id) e religado ao destino.
     * Livro e Movimentacao nao sao acessados - estado e emprestimo ficam intactos.
     */
    public synchronized void moverLivro(String idLivro, GrupoDoAcervo destino) {
        GrupoDoAcervo origem = grupoDoLivro.get(idLivro);
        if (origem == null) {
            throw new RegraDeNegocioException("Livro " + idLivro + " nao esta organizado em nenhum grupo");
        }
        if (origem == destino) {
            return;
        }
        ItemDoAcervo item = origem.retirarLivro(idLivro);
        destino.receber(item);
        grupoDoLivro.put(idLivro, destino);
    }

    public synchronized Optional<GrupoDoAcervo> grupoDo(String idLivro) {
        return Optional.ofNullable(grupoDoLivro.get(idLivro));
    }

    public synchronized Optional<GrupoDoAcervo> unidade(String nome) {
        return Optional.ofNullable(unidades.get(nome));
    }

    /** Procura um grupo pelo nome em todas as unidades (percurso em profundidade). */
    public synchronized Optional<GrupoDoAcervo> buscarGrupo(String nome) {
        for (GrupoDoAcervo unidade : unidades.values()) {
            Optional<GrupoDoAcervo> achado = buscar(unidade, nome);
            if (achado.isPresent()) {
                return achado;
            }
        }
        return Optional.empty();
    }

    private Optional<GrupoDoAcervo> buscar(GrupoDoAcervo grupo, String nome) {
        if (grupo.getNome().equalsIgnoreCase(nome)) {
            return Optional.of(grupo);
        }
        for (NoDoAcervo filho : grupo.getFilhos()) {
            if (filho instanceof GrupoDoAcervo g) {
                Optional<GrupoDoAcervo> achado = buscar(g, nome);
                if (achado.isPresent()) {
                    return achado;
                }
            }
        }
        return Optional.empty();
    }

    public synchronized Collection<GrupoDoAcervo> unidades() {
        return java.util.List.copyOf(unidades.values());
    }
}
