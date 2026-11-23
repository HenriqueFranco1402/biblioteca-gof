package br.pucpr.biblioteca.app;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.acesso.Movimentacao;
import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.banco.BancoAApi;
import br.pucpr.biblioteca.banco.BancoBApi;
import br.pucpr.biblioteca.cadastro.CadastroDeAtendente;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.CadastroDeLivro;
import br.pucpr.biblioteca.cadastro.CadastroDePessoa;
import br.pucpr.biblioteca.circulacao.FachadaCirculacao;
import br.pucpr.biblioteca.circulacao.solicitacao.ExecutorDeSolicitacoes;
import br.pucpr.biblioteca.consulta.ArmazenamentoCache;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.ConsultaAcervoOriginal;
import br.pucpr.biblioteca.consulta.ConsultaComCache;
import br.pucpr.biblioteca.consulta.ConsultaComMedicao;
import br.pucpr.biblioteca.devolucao.FilaDeInspecao;
import br.pucpr.biblioteca.devolucao.Indicadores;
import br.pucpr.biblioteca.devolucao.OuvinteEmail;
import br.pucpr.biblioteca.devolucao.OuvinteIndicadores;
import br.pucpr.biblioteca.devolucao.OuvinteInspecao;
import br.pucpr.biblioteca.devolucao.OuvinteMulta;
import br.pucpr.biblioteca.devolucao.PublicadorDevolucao;
import br.pucpr.biblioteca.financeiro.ServicoDeMultas;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoA;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoB;
import br.pucpr.biblioteca.financeiro.cobranca.GatewayCobranca;
import br.pucpr.biblioteca.financeiro.politica.ConfiguracaoAdministrativa;
import br.pucpr.biblioteca.notificacao.CaixaDeSaidaEmail;
import br.pucpr.biblioteca.notificacao.ServicoEmail;
import br.pucpr.biblioteca.organizacao.OrganizacaoDoAcervo;
import br.pucpr.biblioteca.persistencia.FabricaDeDados;
import br.pucpr.biblioteca.persistencia.SeletorDeFabrica;
import br.pucpr.biblioteca.tratadores.TratadorDeAcervo;
import br.pucpr.biblioteca.tratadores.TratadorDeAtendente;
import br.pucpr.biblioteca.tratadores.TratadorDeDevolucao;
import br.pucpr.biblioteca.tratadores.TratadorDeEmprestimo;
import br.pucpr.biblioteca.tratadores.TratadorDeEstudante;
import br.pucpr.biblioteca.tratadores.TratadorDeNotificacoes;
import br.pucpr.biblioteca.tratadores.TratadorDePessoa;
import br.pucpr.biblioteca.tratadores.TratadorFinanceiro;

import java.time.Clock;

/**
 * RAIZ DE COMPOSICAO: o unico lugar que conhece as classes concretas e liga tudo.
 *
 * <ul>
 *   <li>Abstract Factory: a familia de persistencia e escolhida UMA vez, pelo perfil.</li>
 *   <li>Adapter: o banco de cobranca e escolhido pela configuracao da instalacao.</li>
 *   <li>Strategy: a politica inicial vem da configuracao administrativa.</li>
 *   <li>Decorator: a cadeia de consulta e montada conforme a instalacao: medicao(cache(original)).</li>
 *   <li>Observer: os quatro ouvintes da devolucao sao registrados no publicador.</li>
 * </ul>
 */
public class Sistema {

    private final ConfiguracaoSistema configuracao;
    private final FabricaDeDados fabrica;
    private final Clock relogio;
    private final ServicoEmail email;

    // pontos internos de acesso
    private final Acervo acervo;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;

    // colaboradores
    private final GatewayCobranca gateway;
    private final ConfiguracaoAdministrativa configuracaoAdministrativa;
    private final ServicoDeMultas servicoDeMultas;
    private final PublicadorDevolucao publicadorDevolucao = new PublicadorDevolucao();
    private final FilaDeInspecao filaDeInspecao = new FilaDeInspecao();
    private final Indicadores indicadores = new Indicadores();
    private final FachadaCirculacao fachada;
    private final ConsultaAcervo consultaAcervo;
    private ConsultaComCache consultaComCache;
    private ConsultaComMedicao consultaComMedicao;

    // tratadores (unico ponto de contato da interface)
    private final TratadorDeAcervo tratadorDeAcervo;
    private final TratadorDePessoa tratadorDePessoa;
    private final TratadorDeEstudante tratadorDeEstudante;
    private final TratadorDeAtendente tratadorDeAtendente;
    private final TratadorDeEmprestimo tratadorDeEmprestimo;
    private final TratadorDeDevolucao tratadorDeDevolucao;
    private final TratadorFinanceiro tratadorFinanceiro;
    private final TratadorDeNotificacoes tratadorDeNotificacoes;

    public Sistema(ConfiguracaoSistema configuracao, Clock relogio) {
        this(configuracao, relogio, new CaixaDeSaidaEmail(),
                SeletorDeFabrica.paraPerfil(configuracao.perfil(), configuracao.bancoDeDados()));
    }

    public Sistema(ConfiguracaoSistema configuracao, Clock relogio, ServicoEmail email, FabricaDeDados fabrica) {
        this.configuracao = configuracao;
        this.relogio = relogio;
        this.email = email;

        // ---- Abstract Factory: familia escolhida uma unica vez ----
        this.fabrica = fabrica;
        fabrica.inicializar();
        this.acervo = new Acervo(fabrica.repositorioLivro());
        this.movimentacao = new Movimentacao(fabrica.repositorioMovimentacao());
        this.relacionamento = new Relacionamento(fabrica.repositorioRelacionamento());

        // ---- Adapter: banco da instalacao ----
        this.gateway = switch (configuracao.banco()) {
            case "B" -> new AdaptadorBancoB(new BancoBApi());
            default -> new AdaptadorBancoA(new BancoAApi());
        };

        // ---- Strategy: politica vigente ----
        this.configuracaoAdministrativa = new ConfiguracaoAdministrativa();
        configuracaoAdministrativa.definirPoliticaVigente(configuracao.politicaMulta());
        this.servicoDeMultas = new ServicoDeMultas(configuracaoAdministrativa, gateway, email, relogio);

        // ---- Observer: ouvintes da devolucao ----
        publicadorDevolucao.registrar(new OuvinteInspecao(filaDeInspecao));
        publicadorDevolucao.registrar(new OuvinteMulta(servicoDeMultas));
        publicadorDevolucao.registrar(new OuvinteIndicadores(indicadores));
        publicadorDevolucao.registrar(new OuvinteEmail(email));

        // ---- Facade ----
        this.fachada = new FachadaCirculacao(fabrica.unidadeDeTrabalho(), acervo, movimentacao, relacionamento,
                publicadorDevolucao, filaDeInspecao, relogio);

        // ---- Decorator: medicao( cache( original ) ) ----
        ConsultaAcervo consulta = new ConsultaAcervoOriginal(acervo);
        if (configuracao.usarCache()) {
            consulta = consultaComCache = new ConsultaComCache(consulta, ArmazenamentoCache.emMemoria());
        }
        if (configuracao.usarMedicao()) {
            consulta = consultaComMedicao = new ConsultaComMedicao(consulta);
        }
        this.consultaAcervo = consulta;

        // ---- Tratadores ----
        this.tratadorDeAcervo = new TratadorDeAcervo(new CadastroDeLivro(fabrica.unidadeDeTrabalho(), acervo),
                consultaAcervo, acervo, new OrganizacaoDoAcervo());
        tratadorDeAcervo.definirEstatisticasDaConsulta(this::estatisticasDaConsulta);
        this.tratadorDePessoa = new TratadorDePessoa(new CadastroDePessoa(fabrica.unidadeDeTrabalho(), relacionamento), relacionamento);
        this.tratadorDeEstudante = new TratadorDeEstudante(new CadastroDeEstudante(fabrica.unidadeDeTrabalho(), relacionamento), relacionamento);
        this.tratadorDeAtendente = new TratadorDeAtendente(new CadastroDeAtendente(fabrica.unidadeDeTrabalho(), relacionamento), relacionamento);
        this.tratadorDeEmprestimo = new TratadorDeEmprestimo(new ExecutorDeSolicitacoes(fachada), fachada);
        this.tratadorDeDevolucao = new TratadorDeDevolucao(fachada, filaDeInspecao);
        this.tratadorFinanceiro = new TratadorFinanceiro(configuracaoAdministrativa, servicoDeMultas, gateway, indicadores, relogio);
        this.tratadorDeNotificacoes = new TratadorDeNotificacoes(email);
    }

    private String estatisticasDaConsulta() {
        StringBuilder sb = new StringBuilder("cadeia: ");
        sb.append(consultaComMedicao != null ? "medicao(" : "").append(consultaComCache != null ? "cache(" : "")
                .append("original").append(consultaComCache != null ? ")" : "").append(consultaComMedicao != null ? ")" : "");
        if (consultaComMedicao != null) {
            sb.append(" | consultas medidas: ").append(consultaComMedicao.totalDeConsultas());
        }
        if (consultaComCache != null) {
            sb.append(" | acertos de cache: ").append(consultaComCache.acertos());
        }
        return sb.toString();
    }

    // ------------------------------ acesso aos tratadores ------------------------------

    public TratadorDeAcervo tratadorDeAcervo() { return tratadorDeAcervo; }

    public TratadorDePessoa tratadorDePessoa() { return tratadorDePessoa; }

    public TratadorDeEstudante tratadorDeEstudante() { return tratadorDeEstudante; }

    public TratadorDeAtendente tratadorDeAtendente() { return tratadorDeAtendente; }

    public TratadorDeEmprestimo tratadorDeEmprestimo() { return tratadorDeEmprestimo; }

    public TratadorDeDevolucao tratadorDeDevolucao() { return tratadorDeDevolucao; }

    public TratadorFinanceiro tratadorFinanceiro() { return tratadorFinanceiro; }

    public TratadorDeNotificacoes tratadorDeNotificacoes() { return tratadorDeNotificacoes; }

    // ------------------- acesso interno (testes e extensoes na montagem) -------------------

    public ConfiguracaoSistema configuracao() { return configuracao; }

    public FabricaDeDados fabrica() { return fabrica; }

    public FachadaCirculacao fachada() { return fachada; }

    public Acervo acervo() { return acervo; }

    public Movimentacao movimentacao() { return movimentacao; }

    public Relacionamento relacionamento() { return relacionamento; }

    public PublicadorDevolucao publicadorDevolucao() { return publicadorDevolucao; }

    public FilaDeInspecao filaDeInspecao() { return filaDeInspecao; }

    public Indicadores indicadores() { return indicadores; }

    public ServicoDeMultas servicoDeMultas() { return servicoDeMultas; }

    public ConfiguracaoAdministrativa configuracaoAdministrativa() { return configuracaoAdministrativa; }

    public GatewayCobranca gateway() { return gateway; }

    public ConsultaAcervo consultaAcervo() { return consultaAcervo; }

    public ServicoEmail email() { return email; }

    public Clock relogio() { return relogio; }
}
