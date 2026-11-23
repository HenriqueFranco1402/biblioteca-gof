package br.pucpr.biblioteca.interfacecli;

import br.pucpr.biblioteca.app.RelogioAjustavel;
import br.pucpr.biblioteca.app.Sistema;
import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.circulacao.Comprovante;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoCirculacao;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoRenovacao;
import br.pucpr.biblioteca.circulacao.solicitacao.SolicitacaoReserva;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.organizacao.TipoGrupo;
import br.pucpr.biblioteca.tratadores.TratadorDeAcervo;
import br.pucpr.biblioteca.tratadores.TratadorDeDevolucao;
import br.pucpr.biblioteca.tratadores.TratadorDeEmprestimo;
import br.pucpr.biblioteca.tratadores.TratadorDeEstudante;
import br.pucpr.biblioteca.tratadores.TratadorDeNotificacoes;
import br.pucpr.biblioteca.tratadores.TratadorDePessoa;
import br.pucpr.biblioteca.tratadores.TratadorFinanceiro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface de linha de comando. Ela SO conversa com os tratadores: nao conhece fachada,
 * repositorios, Livro, Movimentacao nem regras de dominio. Para reserva e renovacao, a tela cria
 * a solicitacao (Command) e a entrega ao TratadorDeEmprestimo.
 */
public class TelaTerminal {

    private static final Pattern TOKENS = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    private final TratadorDeAcervo acervo;
    private final TratadorDePessoa pessoas;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorFinanceiro financeiro;
    private final TratadorDeNotificacoes notificacoes;
    private final RelogioAjustavel relogio;
    private final PrintStream out;
    private final String usuario;
    private SolicitacaoCirculacao ultimaSolicitacao;

    public TelaTerminal(Sistema sistema, RelogioAjustavel relogio, PrintStream out) {
        this.acervo = sistema.tratadorDeAcervo();
        this.pessoas = sistema.tratadorDePessoa();
        this.estudantes = sistema.tratadorDeEstudante();
        this.emprestimos = sistema.tratadorDeEmprestimo();
        this.devolucoes = sistema.tratadorDeDevolucao();
        this.financeiro = sistema.tratadorFinanceiro();
        this.notificacoes = sistema.tratadorDeNotificacoes();
        this.relogio = relogio;
        this.out = out;
        this.usuario = "atendente-terminal";
    }

    public void executar(BufferedReader entrada, boolean eco) throws IOException {
        cabecalho();
        String linha;
        out.print("\nbiblioteca> ");
        while ((linha = entrada.readLine()) != null) {
            if (eco) {
                out.println(linha);
            }
            if (!processar(linha.trim())) {
                break;
            }
            out.print("\nbiblioteca> ");
        }
        out.println("\nAte logo!");
    }

    private void cabecalho() {
        out.println("==============================================================");
        out.println(" Sistema de Biblioteca - PUCPR - Padroes GoF");
        out.println(" Perfil/banco: banco de cobranca " + financeiro.banco() + " | politica de multa "
                + financeiro.politicaVigente() + " | hoje = " + relogio.hoje());
        out.println(" Digite 'ajuda' para ver os comandos ou 'roteiro' para o fluxo da demonstracao.");
        out.println("==============================================================");
    }

    /** @return false para encerrar */
    boolean processar(String linha) {
        if (linha.isEmpty() || linha.startsWith("#")) {
            return true;
        }
        List<String> a = tokens(linha);
        String cmd = a.get(0).toLowerCase();
        try {
            switch (cmd) {
                case "sair", "exit" -> {
                    return false;
                }
                case "ajuda", "help" -> ajuda();
                case "roteiro" -> roteiro();
                case "hoje" -> out.println("Hoje e " + relogio.hoje());
                case "avancar" -> {
                    relogio.avancarDias(Long.parseLong(arg(a, 1)));
                    out.println("Relogio avancado. Hoje e " + relogio.hoje());
                }
                // ---------- cadastros (Template Method) ----------
                case "pessoa" -> out.println(pessoas.incluirPessoa(
                        new Dados.Pessoa(arg(a, 1), arg(a, 2), arg(a, 3), a.size() > 4 ? a.get(4) : null)).resumo());
                case "estudante" -> out.println(estudantes.incluirEstudante(new Dados.Estudante(arg(a, 1), arg(a, 2))).resumo());
                case "livro" -> out.println(acervo.incluirLivro(
                        new Dados.Livro(arg(a, 1), arg(a, 2), a.size() > 3 ? a.get(3) : null)).resumo());
                case "livros" -> acervo.listarLivros().forEach(f -> out.println("  " + f.resumo()));
                case "consultar" -> out.println(acervo.consultarLivro(arg(a, 1)).map(f -> f.resumo()).orElse("Livro nao encontrado"));
                case "estatisticas" -> out.println(acervo.estatisticasDaConsulta());
                // ---------- circulacao (Command + Facade + State) ----------
                case "reservar" -> submeter(new SolicitacaoReserva(SolicitacaoCirculacao.novoId(), usuario, Instant.now(),
                        arg(a, 1), arg(a, 2)));
                case "renovar" -> submeter(new SolicitacaoRenovacao(SolicitacaoCirculacao.novoId(), usuario, Instant.now(),
                        arg(a, 1), arg(a, 2)));
                case "reenviar" -> {
                    if (ultimaSolicitacao == null) {
                        out.println("Nenhuma solicitacao enviada ainda.");
                    } else {
                        out.println("Reenviando a MESMA solicitacao (id " + curto(ultimaSolicitacao.getId()) + ")...");
                        submeter(ultimaSolicitacao);
                    }
                }
                case "emprestar" -> out.println(emprestimos.realizarEmprestimo(arg(a, 1), arg(a, 2)).resumo());
                case "devolver" -> out.println(devolucoes.registrarDevolucao(arg(a, 1), arg(a, 2)).resumo());
                case "inspecionar" -> out.println(devolucoes.registrarInspecao(arg(a, 1),
                        !arg(a, 2).equalsIgnoreCase("danificado")).resumo());
                case "aguardando" -> out.println("Aguardando inspecao: " + devolucoes.livrosAguardandoInspecao());
                case "reparar" -> out.println(devolucoes.enviarParaReparo(arg(a, 1)).resumo());
                case "reparado" -> out.println(devolucoes.concluirReparo(arg(a, 1)).resumo());
                case "laudo" -> out.println(devolucoes.registrarLaudoIrrecuperavel(arg(a, 1)).resumo());
                // ---------- financeiro (Strategy + Adapter) ----------
                case "politica" -> {
                    if (a.size() > 1) {
                        financeiro.definirPoliticaMulta(a.get(1));
                    }
                    out.println("Politica vigente: " + financeiro.politicaVigente() + " | disponiveis: " + financeiro.politicasDisponiveis());
                }
                case "multas" -> {
                    List<String> lista = a.size() > 1 ? financeiro.multasDoEstudante(a.get(1)) : financeiro.todasAsMultas();
                    if (lista.isEmpty()) {
                        out.println("Nenhuma multa.");
                    }
                    lista.forEach(m -> out.println("  " + m));
                }
                case "pagar" -> out.println(financeiro.simularPagamentoNoBanco(arg(a, 1)));
                case "indicadores" -> out.println(financeiro.indicadores());
                case "emails" -> notificacoes.emailsEnviados().forEach(e -> out.println("  " + e));
                // ---------- organizacao (Composite) ----------
                case "unidade" -> out.println("Criado: " + acervo.criarUnidade(arg(a, 1)));
                case "secao" -> out.println("Criado: " + acervo.criarGrupo(arg(a, 1), arg(a, 2), TipoGrupo.SECAO));
                case "subsecao" -> out.println("Criado: " + acervo.criarGrupo(arg(a, 1), arg(a, 2), TipoGrupo.SUBSECAO));
                case "alocar" -> out.println("Livro em: " + acervo.alocarLivro(arg(a, 1), arg(a, 2)));
                case "mover" -> out.println("Livro movido para: " + acervo.moverLivro(arg(a, 1), arg(a, 2)));
                case "contar" -> out.println(arg(a, 1) + " tem " + acervo.contarLivros(arg(a, 1)) + " livro(s): "
                        + acervo.listarLivrosDoGrupo(arg(a, 1)));
                case "arvore" -> acervo.arvore().forEach(out::println);
                default -> out.println("Comando desconhecido: " + cmd + " (digite 'ajuda')");
            }
        } catch (RegraDeNegocioException e) {
            out.println("RECUSADO: " + e.getMessage());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            out.println("ERRO: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("FALHA: " + e);
        }
        return true;
    }

    private void submeter(SolicitacaoCirculacao s) {
        ultimaSolicitacao = s;
        Comprovante c = emprestimos.submeter(s);
        out.println("[solicitacao " + curto(s.getId()) + " por " + s.getAutor() + "] " + c.resumo());
    }

    private static String curto(String id) {
        return id.substring(0, 8);
    }

    private static String arg(List<String> a, int i) {
        if (i >= a.size()) {
            throw new IllegalArgumentException("argumento " + i + " faltando (digite 'ajuda')");
        }
        return a.get(i);
    }

    static List<String> tokens(String linha) {
        List<String> r = new ArrayList<>();
        Matcher m = TOKENS.matcher(linha);
        while (m.find()) {
            r.add(m.group(1) != null ? m.group(1) : m.group(2));
        }
        return r;
    }

    private void ajuda() {
        out.println("""
                Cadastros (Template Method):
                  pessoa <cpf> <nome> <sobrenome> [email]   estudante <idEstudante> <cpf>
                  livro <idLivro> "<titulo>" ["<autor>"]   livros   consultar <idLivro>   estatisticas
                Circulacao (Command / Facade / State):
                  reservar <idLivro> <idEstudante>     emprestar <idLivro> <idEstudante>
                  renovar <idLivro> <idEstudante>      reenviar   (repete a ultima solicitacao com o MESMO id)
                  devolver <idLivro> <idEstudante>     aguardando
                  inspecionar <idLivro> ok|danificado  reparar <idLivro>  reparado <idLivro>  laudo <idLivro>
                Financeiro (Strategy / Adapter / Observer):
                  politica [ORIGINAL|TETO|TOLERANCIA]  multas [idEstudante]  pagar <idMulta>
                  indicadores   emails
                Organizacao do acervo (Composite):
                  unidade <nome>  secao <pai> <nome>  subsecao <pai> <nome>
                  alocar <grupo> <idLivro>  mover <idLivro> <grupoDestino>  contar <grupo>  arvore
                Tempo:  hoje   avancar <dias>          Outros: roteiro  ajuda  sair""");
    }

    private void roteiro() {
        out.println("""
                Fluxo completo sugerido (dados de exemplo ja carregados: estudante E1, livros L1..L5):
                  reservar L1 E1        -> LIVRE -> RESERVADO
                  emprestar L1 E1       -> RESERVADO -> EMPRESTADO (vence em 14 dias)
                  renovar L1 E1         -> +30 dias (1/2)
                  reenviar              -> mesmo id: NAO soma outros 30 dias
                  renovar L1 E1         -> +30 dias (2/2)
                  renovar L1 E1         -> RECUSADO (guarda numRenovacoes < 2)
                  avancar 79            -> passa 5 dias do vencimento
                  devolver L1 E1        -> DEVOLVIDO, 5 dias de atraso; ouvintes: inspecao, multa, indicadores, e-mail
                  multas E1             -> R$ 5,00 (politica ORIGINAL)
                  reservar L1 E1        -> RECUSADO (devolvido ainda nao inspecionado)
                  inspecionar L1 ok     -> LIVRE
                  pagar MUL-1  /  pagar MUL-1   -> segunda notificacao nao quita de novo
                  multas E1   indicadores   emails   arvore""");
    }
}
