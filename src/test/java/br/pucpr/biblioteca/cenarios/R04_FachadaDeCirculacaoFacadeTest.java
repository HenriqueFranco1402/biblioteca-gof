package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.app.ConfiguracaoSistema;
import br.pucpr.biblioteca.app.DadosDeExemplo;
import br.pucpr.biblioteca.app.Sistema;
import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.movimentacao.Transacao;
import br.pucpr.biblioteca.notificacao.CaixaDeSaidaEmail;
import br.pucpr.biblioteca.persistencia.FabricaDeDados;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;
import br.pucpr.biblioteca.persistencia.memoria.FabricaEmMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requisito 4 - Entrada unica para as operacoes de circulacao (Facade). */
@DisplayName("R04 - Facade: fachada de circulacao")
class R04_FachadaDeCirculacaoFacadeTest extends CenarioBase {

    @Test
    @DisplayName("R04 cenario: efetivar emprestimo valida a reserva, remove a reserva, cria o emprestimo e atualiza o Livro")
    void R04_efetivacaoDeEmprestimoCoordenaTudoSemExporColecoes() {
        reservar("L1", "E1");
        assertTrue(sistema.movimentacao().reservaAtiva("L1").isPresent());

        Comprovante.Emprestimo comprovante = sistema.tratadorDeEmprestimo().realizarEmprestimo("L1", "E1");

        assertFalse(sistema.movimentacao().reservaAtiva("L1").isPresent(), "reserva ativa removida");
        Emprestimo emp = sistema.movimentacao().consultarEmprestimo("L1", "E1");
        assertEquals(comprovante.idEmprestimo(), emp.getId(), "emprestimo criado");
        assertEquals("EMPRESTADO", estado("L1"), "Livro atualizado");
        assertEquals(HOJE.plusDays(14), comprovante.vencimento());

        // o comprovante e um valor simples: nenhum componente e colecao/mapa
        for (RecordComponent c : Comprovante.Emprestimo.class.getRecordComponents()) {
            assertFalse(Collection.class.isAssignableFrom(c.getType()) || Map.class.isAssignableFrom(c.getType()),
                    "componente " + c.getName() + " nao pode ser colecao");
        }
    }

    @Test
    @DisplayName("R04: sem reserva do estudante o emprestimo e recusado e nada muda")
    void R04_emprestimoSemReservaRecusado() {
        reservar("L1", "E1");
        assertThrows(RegraDeNegocioException.class, () -> sistema.tratadorDeEmprestimo().realizarEmprestimo("L1", "E2"));
        assertEquals("RESERVADO", estado("L1"));
        assertTrue(sistema.movimentacao().reservaAtiva("L1").isPresent());
    }

    @Test
    @DisplayName("R04: falha no meio da operacao desfaz todos os passos (transacao)")
    void R04_falhaNoMeioDesfazTudo() {
        // fabrica de teste: igual a de memoria, mas a gravacao do Emprestimo (passo 1.5) falha
        FabricaEmMemoria real = new FabricaEmMemoria();
        FabricaDeDados comFalha = new FabricaDeDados() {
            public String familia() { return "memoria-com-falha"; }

            public UnidadeDeTrabalho unidadeDeTrabalho() { return real.unidadeDeTrabalho(); }

            public RepositorioLivro repositorioLivro() { return real.repositorioLivro(); }

            public RepositorioRelacionamento repositorioRelacionamento() { return real.repositorioRelacionamento(); }

            public RepositorioMovimentacao repositorioMovimentacao() {
                RepositorioMovimentacao r = real.repositorioMovimentacao();
                return new RepositorioMovimentacao() {
                    public void adicionar(Transacao t) {
                        if (t instanceof Emprestimo) {
                            throw new IllegalStateException("disco cheio (falha simulada)");
                        }
                        r.adicionar(t);
                    }

                    public void atualizar(Emprestimo e) { r.atualizar(e); }

                    public void remover(Reserva reserva) { r.remover(reserva); }

                    public Optional<Reserva> reservaAtiva(String id) { return r.reservaAtiva(id); }

                    public Optional<Emprestimo> emprestimoAtivo(String id) { return r.emprestimoAtivo(id); }

                    public Optional<Devolucao> ultimaDevolucao(String l, String e) { return r.ultimaDevolucao(l, e); }

                    public List<Transacao> listar() { return r.listar(); }

                    public UnidadeDeTrabalho unidadeDeTrabalho() { return r.unidadeDeTrabalho(); }
                };
            }
        };
        Sistema s = new Sistema(ConfiguracaoSistema.paraTestes(), relogio, new CaixaDeSaidaEmail(), comFalha);
        DadosDeExemplo.carregar(s);
        s.fachada().reservar("L1", "E1");

        assertThrows(IllegalStateException.class, () -> s.fachada().efetivarEmprestimo("L1", "E1"));

        assertEquals("RESERVADO", s.acervo().consultarLivro("L1").getNomeEstado(), "estado do livro desfeito");
        assertTrue(s.movimentacao().reservaAtiva("L1").isPresent(), "remocao da reserva desfeita");
        assertFalse(s.movimentacao().emprestimoAtivo("L1").isPresent());
    }
}
