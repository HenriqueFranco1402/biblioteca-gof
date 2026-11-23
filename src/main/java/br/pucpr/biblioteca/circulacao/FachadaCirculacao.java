package br.pucpr.biblioteca.circulacao;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.acesso.Movimentacao;
import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.devolucao.EventoDevolucao;
import br.pucpr.biblioteca.devolucao.FilaDeInspecao;
import br.pucpr.biblioteca.devolucao.PublicadorDevolucao;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.time.Clock;
import java.time.LocalDate;

/**
 * PADRAO FACADE - papel: Fachada de circulacao.
 *
 * <p>Entrada unica para as operacoes de circulacao. Coordena Relacionamento, Movimentacao, Acervo
 * e Livro em operacoes COMPLETAS e TRANSACIONAIS (todas dentro de {@link UnidadeDeTrabalho#executar}):
 * se qualquer passo falhar no meio, a unidade de trabalho desfaz tudo o que a operacao gravou.
 * Devolve {@link Comprovante}s, nunca colecoes. Os tratadores delegam a ela e a interface conhece
 * apenas os tratadores.</p>
 *
 * <p>Nao ha regra de ciclo de vida aqui: a fachada so chama os eventos do Livro (State), que
 * aceitam ou recusam a operacao.</p>
 */
public class FachadaCirculacao {

    private final UnidadeDeTrabalho unidadeDeTrabalho;
    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final PublicadorDevolucao publicadorDevolucao;
    private final FilaDeInspecao filaDeInspecao;
    private final Clock relogio;

    public FachadaCirculacao(UnidadeDeTrabalho unidadeDeTrabalho, Acervo acervo, Movimentacao movimentacao,
                             Relacionamento relacionamento, PublicadorDevolucao publicadorDevolucao,
                             FilaDeInspecao filaDeInspecao, Clock relogio) {
        this.unidadeDeTrabalho = unidadeDeTrabalho;
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.publicadorDevolucao = publicadorDevolucao;
        this.filaDeInspecao = filaDeInspecao;
        this.relogio = relogio;
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    /** Solicitar reserva: valida estudante, Livro LIVRE -> RESERVADO, cria a Reserva. */
    public synchronized Comprovante.Reserva reservar(String idLivro, String idEstudante) {
        return unidadeDeTrabalho.executar(() -> {
            relacionamento.estudanteCadastrado(idEstudante);       // 1. estudante existe
            Livro livro = acervo.consultarLivro(idLivro);           // 2. estado ATUAL do livro
            livro.reservar();                                        // 3. State decide (LIVRE -> RESERVADO)
            Reserva reserva = new Reserva(Ids.novo("RES"), idLivro, idEstudante, hoje());
            movimentacao.adicionar(reserva);                         // 4. registra a reserva
            acervo.atualizar(livro);                                 // 5. grava o novo estado
            return new Comprovante.Reserva(reserva.getId(), idLivro, idEstudante, reserva.getData(), livro.getNomeEstado());
        });
    }

    /**
     * Efetivar emprestimo (diagrama "Emprestar Livro"): 1.1 consultarReserva, 1.2 Livro -> EMPRESTADO,
     * 1.3 removerReserva, 1.4 criar Emprestimo, 1.5 adicionar emprestimo. Tudo ou nada.
     */
    public synchronized Comprovante.Emprestimo efetivarEmprestimo(String idLivro, String idEstudante) {
        return unidadeDeTrabalho.executar(() -> {
            relacionamento.estudanteCadastrado(idEstudante);
            Reserva reserva = movimentacao.consultarReserva(idLivro, idEstudante);   // 1.1 valida a reserva
            Livro livro = acervo.consultarLivro(idLivro);
            livro.emprestar();                                                        // 1.2 RESERVADO -> EMPRESTADO
            movimentacao.removerReserva(reserva);                                     // 1.3
            Emprestimo emprestimo = Emprestimo.novo(Ids.novo("EMP"), idLivro, idEstudante, hoje()); // 1.4
            movimentacao.adicionar(emprestimo);                                       // 1.5
            acervo.atualizar(livro);
            return new Comprovante.Emprestimo(emprestimo.getId(), idLivro, idEstudante, emprestimo.getData(),
                    emprestimo.getDataVencimento(), livro.getNomeEstado());
        });
    }

    /**
     * Renovar emprestimo (diagrama "renovarEmprestimoLivro"): consulta o emprestimo e dispara o evento
     * renovar no Livro. A guarda numRenovacoes &lt; 2 e verificada pelo estado ANTES de mexer no vencimento.
     */
    public synchronized Comprovante.Renovacao renovar(String idLivro, String idEstudante) {
        return unidadeDeTrabalho.executar(() -> {
            Emprestimo emprestimo = movimentacao.consultarEmprestimo(idLivro, idEstudante);  // 1.1
            Livro livro = acervo.consultarLivro(idLivro);
            livro.renovar(emprestimo);                     // 1.2 + 1.3: guarda, vencimento += 30, -> RENOVADO
            movimentacao.atualizar(emprestimo);
            acervo.atualizar(livro);
            return new Comprovante.Renovacao(emprestimo.getId(), idLivro, emprestimo.getDataVencimento(),
                    livro.getNumRenovacoes(), livro.getNomeEstado());
        });
    }

    /** Registrar devolucao na data de hoje (relogio do sistema). */
    public Comprovante.Devolucao registrarDevolucao(String idLivro, String idEstudante) {
        return registrarDevolucao(idLivro, idEstudante, hoje());
    }

    /**
     * Registrar devolucao (diagrama "registrarDevolucaoLivro"). A transacao e CONFIRMADA e so depois
     * o evento e publicado aos ouvintes (Observer): nenhuma falha de ouvinte desfaz a devolucao.
     * O livro fica DEVOLVIDO ate a decisao da inspecao.
     */
    public Comprovante.Devolucao registrarDevolucao(String idLivro, String idEstudante, LocalDate dataDevolucao) {
        ResultadoDevolucao resultado;
        synchronized (this) {
            resultado = unidadeDeTrabalho.executar(() -> {
                Estudante estudante = relacionamento.estudanteCadastrado(idEstudante);
                Emprestimo emprestimo = movimentacao.consultarEmprestimo(idLivro, idEstudante);  // 1.1
                Livro livro = acervo.consultarLivro(idLivro);
                livro.devolver(dataDevolucao);                                 // EMPRESTADO/RENOVADO -> DEVOLVIDO
                Devolucao devolucao = new Devolucao(Ids.novo("DEV"), idLivro, idEstudante, dataDevolucao,
                        emprestimo.getId(), emprestimo.getDataVencimento());  // 1.2 criar
                emprestimo.encerrar();
                movimentacao.atualizar(emprestimo);
                movimentacao.adicionar(devolucao);                             // 1.3 adicionar
                acervo.atualizar(livro);                                       // 1.4 grava estado
                EventoDevolucao ev = new EventoDevolucao(devolucao.getId(), idLivro, livro.getTitulo(), idEstudante,
                        estudante.getCpfPessoa(), estudante.getEmail(), devolucao.getVencimento(), dataDevolucao);
                Comprovante.Devolucao c = new Comprovante.Devolucao(devolucao.getId(), idLivro, idEstudante,
                        devolucao.getVencimento(), dataDevolucao, devolucao.diasDeAtraso(), livro.getNomeEstado());
                return new ResultadoDevolucao(ev, c);
            });                                  // <- commit acontece aqui
        }
        publicadorDevolucao.publicar(resultado.evento());   // somente apos o commit
        return resultado.comprovante();
    }

    private record ResultadoDevolucao(EventoDevolucao evento, Comprovante.Devolucao comprovante) {
    }

    /** Decisao da inspecao: bom estado -> LIVRE; danificado -> REPARANDO. */
    public synchronized Comprovante.SituacaoLivro registrarInspecao(String idLivro, boolean bomEstado) {
        Comprovante.SituacaoLivro c = unidadeDeTrabalho.executar(() -> {
            Livro livro = acervo.consultarLivro(idLivro);
            if (bomEstado) {
                livro.aprovarInspecao();
            } else {
                livro.reprovarInspecao();
            }
            acervo.atualizar(livro);
            return new Comprovante.SituacaoLivro(idLivro, livro.getNomeEstado(), "Inspecao");
        });
        filaDeInspecao.concluir(idLivro);
        return c;
    }

    public synchronized Comprovante.SituacaoLivro enviarParaReparo(String idLivro) {
        return alterarSituacao(idLivro, "Envio para reparo", Livro::enviarParaReparo);
    }

    public synchronized Comprovante.SituacaoLivro concluirReparo(String idLivro) {
        return alterarSituacao(idLivro, "Reparo concluido", Livro::concluirReparo);
    }

    /** Evento de laudo irrecuperavel: REPARANDO -> INCINERADO. */
    public synchronized Comprovante.SituacaoLivro registrarLaudoIrrecuperavel(String idLivro) {
        return alterarSituacao(idLivro, "Laudo irrecuperavel", Livro::registrarLaudoIrrecuperavel);
    }

    private Comprovante.SituacaoLivro alterarSituacao(String idLivro, String operacao,
                                                       java.util.function.Consumer<Livro> evento) {
        return unidadeDeTrabalho.executar(() -> {
            Livro livro = acervo.consultarLivro(idLivro);
            evento.accept(livro);
            acervo.atualizar(livro);
            return new Comprovante.SituacaoLivro(idLivro, livro.getNomeEstado(), operacao);
        });
    }
}
