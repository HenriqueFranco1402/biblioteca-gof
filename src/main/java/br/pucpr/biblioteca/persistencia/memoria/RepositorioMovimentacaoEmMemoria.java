package br.pucpr.biblioteca.persistencia.memoria;

import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.movimentacao.Transacao;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia memoria). */
public final class RepositorioMovimentacaoEmMemoria implements RepositorioMovimentacao {

    private final UnidadeDeTrabalhoEmMemoria uow;

    RepositorioMovimentacaoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.uow = uow;
    }

    @Override
    public void adicionar(Transacao t) {
        uow.base().transacoes.put(t.getId(), paraRegistro(t));
    }

    @Override
    public void atualizar(Emprestimo e) {
        uow.base().transacoes.put(e.getId(), paraRegistro(e));
    }

    @Override
    public void remover(Reserva reserva) {
        uow.base().transacoes.remove(reserva.getId());
    }

    @Override
    public Optional<Reserva> reservaAtiva(String idLivro) {
        return registros().filter(r -> r.tipo().equals("RESERVA") && r.idLivro().equals(idLivro))
                .findFirst().map(r -> (Reserva) paraDominio(r));
    }

    @Override
    public Optional<Emprestimo> emprestimoAtivo(String idLivro) {
        return registros().filter(r -> r.tipo().equals("EMPRESTIMO") && r.ativo() && r.idLivro().equals(idLivro))
                .findFirst().map(r -> (Emprestimo) paraDominio(r));
    }

    @Override
    public Optional<Devolucao> ultimaDevolucao(String idLivro, String idEstudante) {
        return registros().filter(r -> r.tipo().equals("DEVOLUCAO") && r.idLivro().equals(idLivro)
                        && r.idEstudante().equals(idEstudante))
                .reduce((primeira, segunda) -> segunda).map(r -> (Devolucao) paraDominio(r));
    }

    @Override
    public List<Transacao> listar() {
        return registros().map(RepositorioMovimentacaoEmMemoria::paraDominio).toList();
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    private Stream<BaseEmMemoria.TransacaoRegistro> registros() {
        return uow.base().transacoes.values().stream();
    }

    private static BaseEmMemoria.TransacaoRegistro paraRegistro(Transacao t) {
        if (t instanceof Emprestimo e) {
            return new BaseEmMemoria.TransacaoRegistro(e.getId(), "EMPRESTIMO", e.getIdLivro(), e.getIdEstudante(),
                    e.getData(), e.getDataVencimento(), e.isAtivo(), null);
        }
        if (t instanceof Devolucao d) {
            return new BaseEmMemoria.TransacaoRegistro(d.getId(), "DEVOLUCAO", d.getIdLivro(), d.getIdEstudante(),
                    d.getData(), d.getVencimento(), false, d.getIdEmprestimo());
        }
        return new BaseEmMemoria.TransacaoRegistro(t.getId(), "RESERVA", t.getIdLivro(), t.getIdEstudante(),
                t.getData(), null, true, null);
    }

    private static Transacao paraDominio(BaseEmMemoria.TransacaoRegistro r) {
        return switch (r.tipo()) {
            case "EMPRESTIMO" -> new Emprestimo(r.id(), r.idLivro(), r.idEstudante(), r.data(), r.vencimento(), r.ativo());
            case "DEVOLUCAO" -> new Devolucao(r.id(), r.idLivro(), r.idEstudante(), r.data(), r.idEmprestimo(), r.vencimento());
            default -> new Reserva(r.id(), r.idLivro(), r.idEstudante(), r.data());
        };
    }
}
