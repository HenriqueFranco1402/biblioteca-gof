package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.dominio.movimentacao.Devolucao;
import br.pucpr.biblioteca.dominio.movimentacao.Emprestimo;
import br.pucpr.biblioteca.dominio.movimentacao.Reserva;
import br.pucpr.biblioteca.dominio.movimentacao.Transacao;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia relacional / MySQL). Tabela unica "transacao" com coluna "tipo". */
public final class RepositorioMovimentacaoJdbc implements RepositorioMovimentacao {

    private static final String INSERT = "INSERT INTO transacao (id, tipo, id_livro, id_estudante, data_transacao, "
            + "vencimento, ativo, id_emprestimo, sequencia) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final UnidadeDeTrabalhoJdbc uow;
    private final Jdbc jdbc;

    RepositorioMovimentacaoJdbc(UnidadeDeTrabalhoJdbc uow) {
        this.uow = uow;
        this.jdbc = new Jdbc(uow);
    }

    @Override
    public void adicionar(Transacao t) {
        int sequencia = jdbc.primeiro("SELECT COALESCE(MAX(sequencia), 0) + 1 AS proxima FROM transacao",
                rs -> rs.getInt("proxima")).orElse(1);
        if (t instanceof Emprestimo e) {
            jdbc.executar(INSERT, e.getId(), "EMPRESTIMO", e.getIdLivro(), e.getIdEstudante(), e.getData(),
                    e.getDataVencimento(), e.isAtivo(), null, sequencia);
        } else if (t instanceof Devolucao d) {
            jdbc.executar(INSERT, d.getId(), "DEVOLUCAO", d.getIdLivro(), d.getIdEstudante(), d.getData(),
                    d.getVencimento(), false, d.getIdEmprestimo(), sequencia);
        } else {
            jdbc.executar(INSERT, t.getId(), "RESERVA", t.getIdLivro(), t.getIdEstudante(), t.getData(),
                    null, true, null, sequencia);
        }
    }

    @Override
    public void atualizar(Emprestimo e) {
        jdbc.executar("UPDATE transacao SET vencimento = ?, ativo = ? WHERE id = ?",
                e.getDataVencimento(), e.isAtivo(), e.getId());
    }

    @Override
    public void remover(Reserva reserva) {
        jdbc.executar("DELETE FROM transacao WHERE id = ? AND tipo = 'RESERVA'", reserva.getId());
    }

    @Override
    public Optional<Reserva> reservaAtiva(String idLivro) {
        return jdbc.primeiro("SELECT * FROM transacao WHERE tipo = 'RESERVA' AND id_livro = ?",
                rs -> (Reserva) mapear(rs), idLivro);
    }

    @Override
    public Optional<Emprestimo> emprestimoAtivo(String idLivro) {
        return jdbc.primeiro("SELECT * FROM transacao WHERE tipo = 'EMPRESTIMO' AND ativo = TRUE AND id_livro = ?",
                rs -> (Emprestimo) mapear(rs), idLivro);
    }

    @Override
    public Optional<Devolucao> ultimaDevolucao(String idLivro, String idEstudante) {
        return jdbc.primeiro("SELECT * FROM transacao WHERE tipo = 'DEVOLUCAO' AND id_livro = ? AND id_estudante = ? "
                + "ORDER BY sequencia DESC", rs -> (Devolucao) mapear(rs), idLivro, idEstudante);
    }

    @Override
    public List<Transacao> listar() {
        return jdbc.listar("SELECT * FROM transacao ORDER BY sequencia", RepositorioMovimentacaoJdbc::mapear);
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    private static Transacao mapear(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String idLivro = rs.getString("id_livro");
        String idEstudante = rs.getString("id_estudante");
        return switch (rs.getString("tipo")) {
            case "EMPRESTIMO" -> new Emprestimo(id, idLivro, idEstudante, Jdbc.data(rs, "data_transacao"),
                    Jdbc.data(rs, "vencimento"), rs.getBoolean("ativo"));
            case "DEVOLUCAO" -> new Devolucao(id, idLivro, idEstudante, Jdbc.data(rs, "data_transacao"),
                    rs.getString("id_emprestimo"), Jdbc.data(rs, "vencimento"));
            default -> new Reserva(id, idLivro, idEstudante, Jdbc.data(rs, "data_transacao"));
        };
    }
}
