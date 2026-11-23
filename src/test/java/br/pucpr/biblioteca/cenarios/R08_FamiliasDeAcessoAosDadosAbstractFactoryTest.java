package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.app.ConfiguracaoSistema;
import br.pucpr.biblioteca.app.DadosDeExemplo;
import br.pucpr.biblioteca.app.RelogioAjustavel;
import br.pucpr.biblioteca.app.Sistema;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoCirculacao;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoReserva;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.notificacao.CaixaDeSaidaEmail;
import br.pucpr.biblioteca.persistencia.ConfiguracaoBanco;
import br.pucpr.biblioteca.persistencia.FabricaDeDados;
import br.pucpr.biblioteca.persistencia.Perfil;
import br.pucpr.biblioteca.persistencia.SeletorDeFabrica;
import br.pucpr.biblioteca.persistencia.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.persistencia.memoria.RepositorioLivroEmMemoria;
import br.pucpr.biblioteca.persistencia.memoria.RepositorioMovimentacaoEmMemoria;
import br.pucpr.biblioteca.persistencia.memoria.RepositorioRelacionamentoEmMemoria;
import br.pucpr.biblioteca.persistencia.memoria.UnidadeDeTrabalhoEmMemoria;
import br.pucpr.biblioteca.persistencia.relacional.FabricaRelacional;
import br.pucpr.biblioteca.persistencia.relacional.RepositorioLivroJdbc;
import br.pucpr.biblioteca.persistencia.relacional.RepositorioMovimentacaoJdbc;
import br.pucpr.biblioteca.persistencia.relacional.RepositorioRelacionamentoJdbc;
import br.pucpr.biblioteca.persistencia.relacional.UnidadeDeTrabalhoJdbc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 8 - Familias compativeis de acesso aos dados (Abstract Factory). */
@DisplayName("R08 - Abstract Factory: familias de persistencia")
class R08_FamiliasDeAcessoAosDadosAbstractFactoryTest {

    private static final ConfiguracaoBanco MYSQL = ConfiguracaoBanco.mysqlPadrao();

    @Test
    @DisplayName("R08 cenario: configuracao de teste fornece apenas objetos em memoria")
    void R08_configuracaoDeTesteForneceApenasObjetosEmMemoria() {
        FabricaDeDados f = SeletorDeFabrica.paraPerfil(Perfil.TESTE, MYSQL);

        assertInstanceOf(FabricaEmMemoria.class, f);
        assertInstanceOf(UnidadeDeTrabalhoEmMemoria.class, f.unidadeDeTrabalho());
        assertInstanceOf(RepositorioLivroEmMemoria.class, f.repositorioLivro());
        assertInstanceOf(RepositorioMovimentacaoEmMemoria.class, f.repositorioMovimentacao());
        assertInstanceOf(RepositorioRelacionamentoEmMemoria.class, f.repositorioRelacionamento());
        assertSame(f.unidadeDeTrabalho(), f.repositorioLivro().unidadeDeTrabalho());
    }

    @Test
    @DisplayName("R08 cenario: configuracao de producao fornece repositorios MySQL que compartilham a mesma unidade de trabalho")
    void R08_configuracaoDeProducaoForneceRepositoriosMySqlComMesmaUnidadeDeTrabalho() {
        FabricaDeDados f = SeletorDeFabrica.paraPerfil(Perfil.PRODUCAO, MYSQL);

        assertInstanceOf(FabricaRelacional.class, f);
        assertInstanceOf(UnidadeDeTrabalhoJdbc.class, f.unidadeDeTrabalho());
        assertInstanceOf(RepositorioLivroJdbc.class, f.repositorioLivro());
        assertInstanceOf(RepositorioMovimentacaoJdbc.class, f.repositorioMovimentacao());
        assertInstanceOf(RepositorioRelacionamentoJdbc.class, f.repositorioRelacionamento());
        assertSame(f.unidadeDeTrabalho(), f.repositorioLivro().unidadeDeTrabalho());
        assertSame(f.unidadeDeTrabalho(), f.repositorioMovimentacao().unidadeDeTrabalho());
        assertSame(f.unidadeDeTrabalho(), f.repositorioRelacionamento().unidadeDeTrabalho());
        assertFalse(((UnidadeDeTrabalhoJdbc) f.unidadeDeTrabalho()).conexaoAberta(), "conexao so abre quando usada");
    }

    @Test
    @DisplayName("R08: o Oracle e replica e nao recebe escritas da aplicacao")
    void R08_oracleRecusadoComoBancoDeEscrita() {
        assertThrows(IllegalArgumentException.class, () -> new FabricaRelacional(
                new ConfiguracaoBanco("jdbc:oracle:thin:@srvBD02:1521/replica", "app", "x")));
    }

    @Test
    @DisplayName("R08: nao e possivel misturar repositorio de uma familia com a transacao da outra")
    void R08_naoHaComoMisturarFamilias() {
        for (Class<?> repo : new Class<?>[]{RepositorioLivroJdbc.class, RepositorioMovimentacaoJdbc.class, RepositorioRelacionamentoJdbc.class}) {
            Constructor<?> c = repo.getDeclaredConstructors()[0];
            assertEquals(UnidadeDeTrabalhoJdbc.class, c.getParameterTypes()[0]);
            assertFalse(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "so a fabrica cria");
        }
        for (Class<?> repo : new Class<?>[]{RepositorioLivroEmMemoria.class, RepositorioMovimentacaoEmMemoria.class, RepositorioRelacionamentoEmMemoria.class}) {
            Constructor<?> c = repo.getDeclaredConstructors()[0];
            assertEquals(UnidadeDeTrabalhoEmMemoria.class, c.getParameterTypes()[0]);
            assertFalse(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "so a fabrica cria");
        }
    }

    @Test
    @DisplayName("R08: familia relacional (H2 em modo MySQL) executa o fluxo e a transacao e compartilhada")
    void R08_familiaRelacionalExecutaFluxoComTransacaoCompartilhada() {
        String url = "jdbc:h2:mem:r08;MODE=MySQL;DB_CLOSE_DELAY=-1";
        FabricaRelacional fabrica = new FabricaRelacional(new ConfiguracaoBanco(url, "sa", ""));
        RelogioAjustavel relogio = RelogioAjustavel.em(LocalDate.of(2026, 11, 2));
        Sistema s = new Sistema(ConfiguracaoSistema.paraTestes(), relogio, new CaixaDeSaidaEmail(), fabrica);
        DadosDeExemplo.carregar(s);

        s.tratadorDeEmprestimo().submeter(new SolicitacaoReserva(SolicitacaoCirculacao.novoId(), "t", Instant.now(), "L1", "E1"));
        s.tratadorDeEmprestimo().realizarEmprestimo("L1", "E1");

        // outra fabrica = outra conexao: enxerga o que foi confirmado
        FabricaRelacional outra = new FabricaRelacional(new ConfiguracaoBanco(url, "sa", ""));
        assertEquals("EMPRESTADO", outra.repositorioLivro().buscar("L1").orElseThrow().getNomeEstado());
        assertTrue(outra.repositorioMovimentacao().emprestimoAtivo("L1").isPresent());
        assertFalse(outra.repositorioMovimentacao().reservaAtiva("L1").isPresent());

        // rollback: livro + movimentacao gravados na mesma transacao sao desfeitos juntos
        assertThrows(IllegalStateException.class, () -> fabrica.unidadeDeTrabalho().executar(() -> {
            fabrica.repositorioLivro().salvar(Livro.novo("LX", "Temporario", null));
            fabrica.repositorioRelacionamento().salvarPessoa(new br.pucpr.biblioteca.dominio.pessoa.Pessoa(
                    "98765432100", "T", "T", null));
            throw new IllegalStateException("falha simulada");
        }));
        assertTrue(outra.repositorioLivro().buscar("LX").isEmpty());
        assertTrue(outra.repositorioRelacionamento().pessoaPorCpf("98765432100").isEmpty());
    }
}
