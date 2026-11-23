package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.organizacao.GrupoDoAcervo;
import br.pucpr.biblioteca.organizacao.NoDoAcervo;
import br.pucpr.biblioteca.organizacao.OrganizacaoDoAcervo;
import br.pucpr.biblioteca.organizacao.TipoGrupo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Requisito 10 - Organizacao hierarquica do acervo (Composite). */
@DisplayName("R10 - Composite: organizacao hierarquica do acervo")
class R10_OrganizacaoHierarquicaCompositeTest extends CenarioBase {

    @Test
    @DisplayName("R10 cenario: unidade com duas secoes (3 e 2 livros) informa total de 5")
    void R10_unidadeComDuasSecoesInformaCincoLivros() {
        OrganizacaoDoAcervo org = new OrganizacaoDoAcervo();
        GrupoDoAcervo unidade = org.criarUnidade("Campus Londrina");
        GrupoDoAcervo secaoA = unidade.adicionar(org.novoGrupo("Secao A", TipoGrupo.SECAO));
        GrupoDoAcervo secaoB = unidade.adicionar(org.novoGrupo("Secao B", TipoGrupo.SECAO));
        secaoA.adicionarLivro("L1");
        secaoA.adicionarLivro("L2");
        secaoA.adicionarLivro("L3");
        secaoB.adicionarLivro("L4");
        secaoB.adicionarLivro("L5");

        assertEquals(5, unidade.contarLivros());
        assertEquals(3, secaoA.contarLivros());
        assertEquals(List.of("L1", "L2", "L3", "L4", "L5"), unidade.listarLivros());
        // grupos e itens respondem a mesma operacao
        NoDoAcervo item = secaoB.getFilhos().get(0);
        assertEquals(1, item.contarLivros());
    }

    @Test
    @DisplayName("R10 cenario: mover livro entre secoes nao altera id, estado nem status de emprestimo")
    void R10_moverLivroNaoAlteraIdEstadoNemEmprestimo() {
        reservarEEmprestar("L4", "E1");   // L4 esta em "Matematica" nos dados de exemplo
        Livro antes = sistema.acervo().consultarLivro("L4");
        var emprestimoAntes = sistema.movimentacao().consultarEmprestimo("L4", "E1");
        int transacoesAntes = sistema.movimentacao().totalDeTransacoes();

        String destino = sistema.tratadorDeAcervo().moverLivro("L4", "Computacao");

        Livro depois = sistema.acervo().consultarLivro("L4");
        assertEquals("Campus Curitiba > Computacao", destino);
        assertEquals(antes.getIdLivro(), depois.getIdLivro());
        assertEquals("EMPRESTADO", depois.getNomeEstado());
        assertEquals(emprestimoAntes.getId(), sistema.movimentacao().consultarEmprestimo("L4", "E1").getId());
        assertEquals(transacoesAntes, sistema.movimentacao().totalDeTransacoes(), "Movimentacao nao foi tocada");
        assertEquals(5, sistema.tratadorDeAcervo().contarLivros("Campus Curitiba"));
        assertEquals(1, sistema.tratadorDeAcervo().contarLivros("Matematica"));
    }

    @Test
    @DisplayName("R10: o grupo recusa ciclos")
    void R10_grupoRecusaCiclos() {
        OrganizacaoDoAcervo org = new OrganizacaoDoAcervo();
        GrupoDoAcervo unidade = org.criarUnidade("U");
        GrupoDoAcervo secao = unidade.adicionar(org.novoGrupo("S", TipoGrupo.SECAO));
        GrupoDoAcervo sub = secao.adicionar(org.novoGrupo("SS", TipoGrupo.SUBSECAO));

        assertThrows(RegraDeNegocioException.class, () -> sub.adicionar(unidade), "ancestral como filho");
        assertThrows(RegraDeNegocioException.class, () -> secao.adicionar(secao), "a si mesmo");
        assertThrows(RegraDeNegocioException.class, () -> unidade.adicionar(sub), "grupo que ja tem pai");
    }

    @Test
    @DisplayName("R10: o grupo recusa livro que ja pertence a outro grupo (inclusive em outra unidade)")
    void R10_livroEmOutroGrupoRecusado() {
        OrganizacaoDoAcervo org = new OrganizacaoDoAcervo();
        GrupoDoAcervo u1 = org.criarUnidade("U1");
        GrupoDoAcervo u2 = org.criarUnidade("U2");
        u1.adicionarLivro("L1");

        assertThrows(RegraDeNegocioException.class, () -> u2.adicionarLivro("L1"));
        assertThrows(RegraDeNegocioException.class, () -> u1.adicionarLivro("L1"));
        assertSame(u1, org.grupoDo("L1").orElseThrow());
    }

    @Test
    @DisplayName("R10: profundidade variavel - a contagem percorre toda a arvore")
    void R10_profundidadeVariavel() {
        OrganizacaoDoAcervo org = new OrganizacaoDoAcervo();
        GrupoDoAcervo g = org.criarUnidade("U");
        for (int nivel = 1; nivel <= 5; nivel++) {
            g = g.adicionar(org.novoGrupo("N" + nivel, TipoGrupo.SUBSECAO));
            g.adicionarLivro("X" + nivel);
        }
        assertEquals(5, org.unidade("U").orElseThrow().contarLivros());
    }
}
