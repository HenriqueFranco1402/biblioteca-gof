package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.cadastro.CadastroDeLivro;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;
import br.pucpr.biblioteca.componentes.iLivro;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.MetadadosLivro;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.organizacao.GrupoDoAcervo;
import br.pucpr.biblioteca.organizacao.NoDoAcervo;
import br.pucpr.biblioteca.organizacao.OrganizacaoDoAcervo;
import br.pucpr.biblioteca.organizacao.TipoGrupo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tratador do acervo (componente Biblioteca, implementa {@link iLivro}).
 * Delega: inclusao ao Template Method, consulta de metadados a cadeia de Decorators e a
 * organizacao hierarquica ao Composite. Nao contem regras de dominio.
 */
public class TratadorDeAcervo implements iLivro {

    private final CadastroDeLivro cadastro;
    private final ConsultaAcervo consulta;     // cadeia de decoradores montada pelo Sistema
    private final Acervo acervo;               // estado ATUAL do livro (nunca do cache)
    private final OrganizacaoDoAcervo organizacao;
    private java.util.function.Supplier<String> estatisticasDaConsulta = () -> "sem decoradores de medicao/cache";

    public void definirEstatisticasDaConsulta(java.util.function.Supplier<String> fonte) {
        this.estatisticasDaConsulta = fonte;
    }

    public String estatisticasDaConsulta() {
        return estatisticasDaConsulta.get();
    }

    public TratadorDeAcervo(CadastroDeLivro cadastro, ConsultaAcervo consulta, Acervo acervo,
                            OrganizacaoDoAcervo organizacao) {
        this.cadastro = cadastro;
        this.consulta = consulta;
        this.acervo = acervo;
        this.organizacao = organizacao;
    }

    @Override
    public ResultadoCadastro incluirLivro(Dados.Livro dados) {
        return cadastro.incluir(dados);
    }

    /** Metadados vem da cadeia (pode ser cache); o estado vem sempre do Acervo. */
    @Override
    public Optional<FichaLivro> consultarLivro(String idLivro) {
        Optional<MetadadosLivro> metadados = consulta.consultarMetadados(idLivro);
        return metadados.flatMap(m -> acervo.buscar(idLivro)
                .map(l -> new FichaLivro(m.idLivro(), m.titulo(), m.autor(), l.getNomeEstado(), l.getNumRenovacoes())));
    }

    @Override
    public List<FichaLivro> listarLivros() {
        return acervo.listar().stream().map(TratadorDeAcervo::ficha).toList();
    }

    private static FichaLivro ficha(Livro l) {
        return new FichaLivro(l.getIdLivro(), l.getTitulo(), l.getAutor(), l.getNomeEstado(), l.getNumRenovacoes());
    }

    // ------------------------- organizacao hierarquica (Composite) -------------------------

    public String criarUnidade(String nome) {
        return organizacao.criarUnidade(nome).caminho();
    }

    public String criarGrupo(String nomeDoPai, String nome, TipoGrupo tipo) {
        GrupoDoAcervo pai = grupo(nomeDoPai);
        return pai.adicionar(organizacao.novoGrupo(nome, tipo)).caminho();
    }

    public String alocarLivro(String nomeDoGrupo, String idLivro) {
        acervo.consultarLivro(idLivro);  // o livro precisa existir no acervo
        return grupo(nomeDoGrupo).adicionarLivro(idLivro).caminho();
    }

    public String moverLivro(String idLivro, String nomeDoGrupoDestino) {
        organizacao.moverLivro(idLivro, grupo(nomeDoGrupoDestino));
        return organizacao.grupoDo(idLivro).orElseThrow().caminho();
    }

    public int contarLivros(String nomeDoGrupo) {
        return grupo(nomeDoGrupo).contarLivros();
    }

    public List<String> listarLivrosDoGrupo(String nomeDoGrupo) {
        return grupo(nomeDoGrupo).listarLivros();
    }

    /** Desenho da arvore para a interface. */
    public List<String> arvore() {
        List<String> linhas = new ArrayList<>();
        for (GrupoDoAcervo unidade : organizacao.unidades()) {
            desenhar(unidade, "", linhas);
        }
        return linhas;
    }

    private void desenhar(NoDoAcervo no, String recuo, List<String> linhas) {
        if (no instanceof GrupoDoAcervo g) {
            linhas.add(recuo + "[" + g.getTipo() + "] " + g.getNome() + " (" + g.contarLivros() + " livro(s))");
            g.getFilhos().forEach(f -> desenhar(f, recuo + "   ", linhas));
        } else {
            linhas.add(recuo + "- " + no.getNome());
        }
    }

    private GrupoDoAcervo grupo(String nome) {
        return organizacao.buscarGrupo(nome).orElseThrow(() -> new RegraDeNegocioException("Grupo nao encontrado: " + nome));
    }
}
