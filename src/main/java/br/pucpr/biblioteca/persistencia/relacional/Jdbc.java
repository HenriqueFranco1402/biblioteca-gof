package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.persistencia.PersistenciaException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Utilitario interno: executa SQL na conexao da unidade de trabalho e traduz SQLException. */
final class Jdbc {

    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private final UnidadeDeTrabalhoJdbc uow;

    Jdbc(UnidadeDeTrabalhoJdbc uow) {
        this.uow = uow;
    }

    int executar(String sql, Object... parametros) {
        try (PreparedStatement ps = uow.conexao().prepareStatement(sql)) {
            vincular(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao executar: " + sql, e);
        }
    }

    <T> List<T> listar(String sql, Mapeador<T> mapeador, Object... parametros) {
        try (PreparedStatement ps = uow.conexao().prepareStatement(sql)) {
            vincular(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapeador.mapear(rs));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar: " + sql, e);
        }
    }

    <T> Optional<T> primeiro(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = listar(sql, mapeador, parametros);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** "Upsert" portavel (MySQL e H2): tenta atualizar; se nenhuma linha mudou, insere. */
    void atualizarOuInserir(String update, Object[] parametrosUpdate, String insert, Object[] parametrosInsert) {
        if (executar(update, parametrosUpdate) == 0) {
            executar(insert, parametrosInsert);
        }
    }

    static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date d = rs.getDate(coluna);
        return d == null ? null : d.toLocalDate();
    }

    private static void vincular(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object p = parametros[i];
            if (p == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else if (p instanceof LocalDate d) {
                ps.setDate(i + 1, Date.valueOf(d));
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }
}
