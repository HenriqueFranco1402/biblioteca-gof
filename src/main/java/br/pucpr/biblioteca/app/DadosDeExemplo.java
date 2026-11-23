package br.pucpr.biblioteca.app;

import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.organizacao.TipoGrupo;

/** Carrega dados de exemplo para a demonstracao, usando APENAS os tratadores. */
public final class DadosDeExemplo {

    private DadosDeExemplo() {
    }

    public static void carregar(Sistema s) {
        var pessoas = s.tratadorDePessoa();
        pessoas.incluirPessoa(new Dados.Pessoa("111.444.777-35", "Ana", "Souza", "ana.souza@pucpr.edu.br"));
        pessoas.incluirPessoa(new Dados.Pessoa("529.982.247-25", "Bruno", "Lima", "bruno.lima@pucpr.edu.br"));
        pessoas.incluirPessoa(new Dados.Pessoa("390.533.447-05", "Carla", "Mendes", "carla.mendes@pucpr.br"));

        s.tratadorDeEstudante().incluirEstudante(new Dados.Estudante("E1", "11144477735"));
        s.tratadorDeEstudante().incluirEstudante(new Dados.Estudante("E2", "52998224725"));
        s.tratadorDeAtendente().incluirAtendente(new Dados.Atendente("A1", "39053344705"));

        var acervo = s.tratadorDeAcervo();
        acervo.incluirLivro(new Dados.Livro("L1", "Padroes de Projeto", "Gamma et al."));
        acervo.incluirLivro(new Dados.Livro("L2", "Engenharia de Software", "Sommerville"));
        acervo.incluirLivro(new Dados.Livro("L3", "UML Essencial", "Fowler"));
        acervo.incluirLivro(new Dados.Livro("L4", "Calculo A", "Flemming"));
        acervo.incluirLivro(new Dados.Livro("L5", "Algebra Linear", "Boldrini"));

        acervo.criarUnidade("Campus Curitiba");
        acervo.criarGrupo("Campus Curitiba", "Computacao", TipoGrupo.SECAO);
        acervo.criarGrupo("Campus Curitiba", "Matematica", TipoGrupo.SECAO);
        acervo.criarGrupo("Computacao", "Engenharia de Software", TipoGrupo.SUBSECAO);
        acervo.alocarLivro("Engenharia de Software", "L1");
        acervo.alocarLivro("Engenharia de Software", "L2");
        acervo.alocarLivro("Computacao", "L3");
        acervo.alocarLivro("Matematica", "L4");
        acervo.alocarLivro("Matematica", "L5");
    }
}
