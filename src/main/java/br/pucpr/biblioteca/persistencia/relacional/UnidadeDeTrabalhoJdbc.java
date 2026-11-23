package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.persistencia.ConfiguracaoBanco;
import br.pucpr.biblioteca.persistencia.PersistenciaException;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PADRAO ABSTRACT FACTORY - Produto concreto (familia relacional).
 *
 * <p>Guarda UMA conexao JDBC, aberta sob demanda (a fabrica pode ser criada sem o MySQL no ar).
 * Todos os repositorios relacionais usam {@link #conexao()} desta instancia, logo participam da
 * mesma transacao: iniciar = setAutoCommit(false), confirmar = commit, desfazer = rollback.</p>
 */
public final class UnidadeDeTrabalhoJdbc implements UnidadeDeTrabalho {

    private final ConfiguracaoBanco configuracao;
    private Connection conexao;
    private boolean emAndamento;

    UnidadeDeTrabalhoJdbc(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    /** Conexao compartilhada; abre na primeira utilizacao. */
    Connection conexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                conexao = DriverManager.getConnection(configuracao.url(), configuracao.usuario(), configuracao.senha());
                conexao.setAutoCommit(true);
            }
            return conexao;
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel conectar ao banco principal: " + configuracao.url(), e);
        }
    }

    public boolean conexaoAberta() {
        return conexao != null;
    }

    @Override
    public void iniciar() {
        try {
            conexao().setAutoCommit(false);
            emAndamento = true;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao iniciar transacao", e);
        }
    }

    @Override
    public void confirmar() {
        try {
            conexao.commit();
            conexao.setAutoCommit(true);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao confirmar transacao", e);
        } finally {
            emAndamento = false;
        }
    }

    @Override
    public void desfazer() {
        try {
            if (conexao != null) {
                conexao.rollback();
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao desfazer transacao", e);
        } finally {
            emAndamento = false;
        }
    }

    @Override
    public boolean emAndamento() {
        return emAndamento;
    }
}
