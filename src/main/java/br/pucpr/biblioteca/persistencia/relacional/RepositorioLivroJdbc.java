package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia relacional / MySQL). Estado gravado na coluna "estado" (VARCHAR). */
public final class RepositorioLivroJdbc implements RepositorioLivro {

    private final UnidadeDeTrabalhoJdbc uow;
    private final Jdbc jdbc;

    /** Exige a unidade de trabalho JDBC: nao ha como liga-lo a uma transacao em memoria. */
    RepositorioLivroJdbc(UnidadeDeTrabalhoJdbc uow) {
        this.uow = uow;
        this.jdbc = new Jdbc(uow);
    }

    @Override
    public Optional<Livro> buscar(String idLivro) {
        return jdbc.primeiro("SELECT * FROM livro WHERE id_livro = ?", RepositorioLivroJdbc::mapear, idLivro);
    }

    @Override
    public void salvar(Livro l) {
        jdbc.atualizarOuInserir(
                "UPDATE livro SET titulo = ?, autor = ?, estado = ?, num_renovacoes = ?, data_ultima_devolucao = ? WHERE id_livro = ?",
                new Object[]{l.getTitulo(), l.getAutor(), l.getNomeEstado(), l.getNumRenovacoes(), l.getDataUltimaDevolucao(), l.getIdLivro()},
                "INSERT INTO livro (id_livro, titulo, autor, estado, num_renovacoes, data_ultima_devolucao) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[]{l.getIdLivro(), l.getTitulo(), l.getAutor(), l.getNomeEstado(), l.getNumRenovacoes(), l.getDataUltimaDevolucao()});
    }

    @Override
    public List<Livro> listar() {
        return jdbc.listar("SELECT * FROM livro ORDER BY id_livro", RepositorioLivroJdbc::mapear);
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    private static Livro mapear(ResultSet rs) throws SQLException {
        return Livro.reconstituir(rs.getString("id_livro"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("estado"), rs.getInt("num_renovacoes"), Jdbc.data(rs, "data_ultima_devolucao"));
    }
}
