package br.pucpr.biblioteca.persistencia.relacional;

import br.pucpr.biblioteca.persistencia.ConfiguracaoBanco;
import br.pucpr.biblioteca.persistencia.FabricaDeDados;
import br.pucpr.biblioteca.persistencia.RepositorioLivro;
import br.pucpr.biblioteca.persistencia.RepositorioMovimentacao;
import br.pucpr.biblioteca.persistencia.RepositorioRelacionamento;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.List;

/**
 * PADRAO ABSTRACT FACTORY - papel: Fabrica concreta da familia RELACIONAL (perfil de producao, MySQL).
 *
 * <p>Cria uma unica {@link UnidadeDeTrabalhoJdbc} e liga a ela os tres repositorios: todos usam
 * a mesma conexao e, portanto, a mesma transacao.</p>
 *
 * <p>Regra do Trabalho 02: o Oracle e apenas REPLICA (a replicacao e feita pelo proprio banco).
 * A aplicacao nunca escreve nele, por isso esta fabrica recusa uma URL Oracle.</p>
 */
public final class FabricaRelacional implements FabricaDeDados {

    private static final List<String> ESQUEMA = List.of(
            "CREATE TABLE IF NOT EXISTS livro (id_livro VARCHAR(40) PRIMARY KEY, titulo VARCHAR(200) NOT NULL, "
                    + "autor VARCHAR(200), estado VARCHAR(20) NOT NULL, num_renovacoes INT NOT NULL, "
                    + "data_ultima_devolucao DATE)",
            "CREATE TABLE IF NOT EXISTS transacao (id VARCHAR(60) PRIMARY KEY, tipo VARCHAR(20) NOT NULL, "
                    + "id_livro VARCHAR(40) NOT NULL, id_estudante VARCHAR(40) NOT NULL, data_transacao DATE NOT NULL, "
                    + "vencimento DATE, ativo BOOLEAN, id_emprestimo VARCHAR(60), sequencia INT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS pessoa (cpf VARCHAR(11) PRIMARY KEY, nome VARCHAR(100), "
                    + "sobrenome VARCHAR(100), email VARCHAR(150))",
            "CREATE TABLE IF NOT EXISTS estudante (id_estudante VARCHAR(40) PRIMARY KEY, cpf VARCHAR(11) NOT NULL, "
                    + "nome VARCHAR(100), sobrenome VARCHAR(100), email VARCHAR(150))",
            "CREATE TABLE IF NOT EXISTS atendente (id_atendente VARCHAR(40) PRIMARY KEY, cpf VARCHAR(11) NOT NULL, "
                    + "nome VARCHAR(100), sobrenome VARCHAR(100), email VARCHAR(150))");

    private final UnidadeDeTrabalhoJdbc uow;
    private final RepositorioLivroJdbc livros;
    private final RepositorioMovimentacaoJdbc movimentacao;
    private final RepositorioRelacionamentoJdbc relacionamento;

    public FabricaRelacional(ConfiguracaoBanco configuracao) {
        if (configuracao.url().toLowerCase().startsWith("jdbc:oracle")) {
            throw new IllegalArgumentException(
                    "O Oracle e replica somente leitura: a aplicacao nao grava nele. Configure o MySQL principal.");
        }
        this.uow = new UnidadeDeTrabalhoJdbc(configuracao);
        this.livros = new RepositorioLivroJdbc(uow);
        this.movimentacao = new RepositorioMovimentacaoJdbc(uow);
        this.relacionamento = new RepositorioRelacionamentoJdbc(uow);
    }

    /** Cria as tabelas se nao existirem. Chamado na inicializacao do perfil de producao. */
    @Override
    public void inicializar() {
        Jdbc jdbc = new Jdbc(uow);
        ESQUEMA.forEach(jdbc::executar);
    }

    @Override
    public String familia() {
        return "relacional";
    }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() {
        return uow;
    }

    @Override
    public RepositorioLivro repositorioLivro() {
        return livros;
    }

    @Override
    public RepositorioMovimentacao repositorioMovimentacao() {
        return movimentacao;
    }

    @Override
    public RepositorioRelacionamento repositorioRelacionamento() {
        return relacionamento;
    }
}
