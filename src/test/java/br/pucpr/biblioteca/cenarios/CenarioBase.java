package br.pucpr.biblioteca.cenarios;

import br.pucpr.biblioteca.app.ConfiguracaoSistema;
import br.pucpr.biblioteca.app.DadosDeExemplo;
import br.pucpr.biblioteca.app.RelogioAjustavel;
import br.pucpr.biblioteca.app.Sistema;
import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoCirculacao;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoRenovacao;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoReserva;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Montagem comum dos cenarios: sistema no perfil de TESTE (familia em memoria), relogio fixo em
 * 02/11/2026 e os dados de exemplo (estudantes E1 e E2; livros L1..L5; unidade "Campus Curitiba").
 */
abstract class CenarioBase {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 2);

    protected RelogioAjustavel relogio;
    protected Sistema sistema;

    @BeforeEach
    void montarSistema() {
        relogio = RelogioAjustavel.em(HOJE);
        sistema = new Sistema(ConfiguracaoSistema.paraTestes(), relogio);
        DadosDeExemplo.carregar(sistema);
    }

    protected Comprovante reservar(String idLivro, String idEstudante) {
        return sistema.tratadorDeEmprestimo().submeter(
                new SolicitacaoReserva(SolicitacaoCirculacao.novoId(), "teste", Instant.now(), idLivro, idEstudante));
    }

    protected Comprovante renovar(String idLivro, String idEstudante) {
        return sistema.tratadorDeEmprestimo().submeter(novaRenovacao(idLivro, idEstudante));
    }

    protected SolicitacaoRenovacao novaRenovacao(String idLivro, String idEstudante) {
        return new SolicitacaoRenovacao(SolicitacaoCirculacao.novoId(), "teste", Instant.now(), idLivro, idEstudante);
    }

    protected Comprovante.Emprestimo reservarEEmprestar(String idLivro, String idEstudante) {
        reservar(idLivro, idEstudante);
        return sistema.tratadorDeEmprestimo().realizarEmprestimo(idLivro, idEstudante);
    }

    protected String estado(String idLivro) {
        return sistema.acervo().consultarLivro(idLivro).getNomeEstado();
    }

    protected LocalDate vencimento(String idLivro) {
        return sistema.movimentacao().emprestimoAtivo(idLivro).map(Emprestimo::getDataVencimento).orElseThrow();
    }
}
