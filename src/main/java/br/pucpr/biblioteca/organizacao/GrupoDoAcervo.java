package br.pucpr.biblioteca.organizacao;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.ArrayList;
import java.util.List;

/**
 * PADRAO COMPOSITE - papel: Composto. Contem filhos (grupos ou itens) e implementa contagem e
 * listagem DELEGANDO recursivamente aos filhos (percurso em profundidade).
 *
 * <p>Regras de integridade:</p>
 * <ul>
 *   <li>recusa ciclos: um grupo nao pode receber a si mesmo nem um ancestral seu;</li>
 *   <li>um grupo so pode ter um pai (nao pode ser filho de dois grupos);</li>
 *   <li>recusa livro que ja pertence a outro grupo (indice unico da {@link OrganizacaoDoAcervo}).</li>
 * </ul>
 */
public final class GrupoDoAcervo extends NoDoAcervo {

    private final TipoGrupo tipo;
    private final OrganizacaoDoAcervo organizacao;
    private final List<NoDoAcervo> filhos = new ArrayList<>();

    GrupoDoAcervo(String nome, TipoGrupo tipo, OrganizacaoDoAcervo organizacao) {
        super(nome);
        this.tipo = tipo;
        this.organizacao = organizacao;
    }

    public TipoGrupo getTipo() {
        return tipo;
    }

    /** Adiciona um subgrupo, recusando ciclos e grupos que ja tem pai. */
    public GrupoDoAcervo adicionar(GrupoDoAcervo subgrupo) {
        if (subgrupo.organizacao != organizacao) {
            throw new RegraDeNegocioException("Grupo pertence a outra organizacao do acervo");
        }
        if (subgrupo == this || ehDescendenteDe(subgrupo)) {
            throw new RegraDeNegocioException("Ciclo recusado: '" + subgrupo.getNome()
                    + "' e ancestral de (ou igual a) '" + getNome() + "'");
        }
        if (subgrupo.getPai() != null) {
            throw new RegraDeNegocioException("O grupo '" + subgrupo.getNome() + "' ja pertence a '"
                    + subgrupo.getPai().getNome() + "'");
        }
        filhos.add(subgrupo);
        subgrupo.definirPai(this);
        return subgrupo;
    }

    /** Inclui um livro (pelo id) neste grupo. */
    public ItemDoAcervo adicionarLivro(String idLivro) {
        organizacao.reservarLugar(idLivro, this);          // recusa se ja esta em outro grupo
        ItemDoAcervo item = new ItemDoAcervo(idLivro);
        filhos.add(item);
        item.definirPai(this);
        return item;
    }

    /** Retira o item deste grupo e devolve-o (usado ao mover). */
    ItemDoAcervo retirarLivro(String idLivro) {
        for (NoDoAcervo filho : filhos) {
            if (filho instanceof ItemDoAcervo item && item.getIdLivro().equals(idLivro)) {
                filhos.remove(item);
                item.definirPai(null);
                return item;
            }
        }
        throw new RegraDeNegocioException("Livro " + idLivro + " nao esta em '" + getNome() + "'");
    }

    /** Recoloca um item ja existente (mesmo objeto, mesmo id) neste grupo. */
    void receber(ItemDoAcervo item) {
        filhos.add(item);
        item.definirPai(this);
    }

    /** true se este grupo esta abaixo de {@code possivelAncestral}. */
    private boolean ehDescendenteDe(GrupoDoAcervo possivelAncestral) {
        for (GrupoDoAcervo g = getPai(); g != null; g = g.getPai()) {
            if (g == possivelAncestral) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int contarLivros() {
        int total = 0;
        for (NoDoAcervo filho : filhos) {
            total += filho.contarLivros();     // polimorfismo: item devolve 1, grupo soma os seus
        }
        return total;
    }

    @Override
    public List<String> listarLivros() {
        List<String> ids = new ArrayList<>();
        for (NoDoAcervo filho : filhos) {
            ids.addAll(filho.listarLivros());
        }
        return ids;
    }

    public List<NoDoAcervo> getFilhos() {
        return List.copyOf(filhos);
    }
}
