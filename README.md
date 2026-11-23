# Biblioteca GoF — Sistema de empréstimo de livros com padrões GoF

Trabalho prático de **Modelagem de Sistemas Computacionais** (PUCPR — Bacharelado em Ciência da Computação,
Prof. Edson Emílio Scalabrin, turma A, 2º semestre de 2026). Continuação do Trabalho 02: as dez situações da lista
"Problemas de aplicação dos padrões GoF" implementadas, testadas e demonstráveis.

**Equipe:** Henrique Franco Nishimura de Lima · Rafael Militão Marques · Santiago Rodrigues da Rocha

---

## 1. Como compilar, executar e testar

### Pré-requisitos
* **JDK 17 ou superior** (o código é compilado com `--release 17`; foi testado com JDK 26). A variável `JAVA_HOME`
  deve apontar para ele.
  > Atenção: um Java 8 no `PATH` não serve. Confira com `java -version`.
* **Não é preciso instalar o Maven**: o projeto traz o *Maven Wrapper* (`mvnw` / `mvnw.cmd`), que baixa o Maven 3.9.9 na
  primeira execução.
* MySQL **só** para o perfil de produção (os testes não precisam de banco).

### Rodar os testes (um único comando)

Windows (PowerShell ou CMD):
```powershell
.\mvnw.cmd clean test
```
Linux/macOS:
```bash
./mvnw clean test
```
Resultado esperado: `Tests run: 42, Failures: 0, Errors: 0` e `BUILD SUCCESS`.

Se o `JAVA_HOME` não estiver configurado no PowerShell:
```powershell
$env:JAVA_HOME = "C:\caminho\para\jdk-17"
```

### Executar a interface (linha de comando)

Interativo:
```powershell
.\mvnw.cmd -q compile exec:java
```
Executando o roteiro da demonstração (fluxo completo automático):
```powershell
.\mvnw.cmd -q compile exec:java "-Dexec.args=roteiro-demo.txt"
```
Na interface, digite `ajuda` (lista de comandos) ou `roteiro` (passo a passo da demonstração). O sistema já sobe com
dados de exemplo: pessoas/estudantes **E1** (Ana) e **E2** (Bruno), atendente **A1**, livros **L1..L5** e a unidade
"Campus Curitiba" com seções e subseção.

### Perfil de produção (MySQL)

A família relacional é escolhida pelo perfil. Com um MySQL rodando em `localhost:3306` (usuário `root`, sem senha), basta:
```powershell
.\mvnw.cmd -q compile exec:java "-Dbiblioteca.perfil=producao"
```
Para outro servidor/usuário use `-Dbiblioteca.db.url=...`, `-Dbiblioteca.db.usuario=...`, `-Dbiblioteca.db.senha=...`
(ou edite `src/main/resources/biblioteca.properties`). As tabelas são criadas automaticamente
(`FabricaRelacional.inicializar()`). O Oracle é réplica: a aplicação recusa uma URL `jdbc:oracle` para escrita.

Outras opções de instalação (`biblioteca.properties` ou `-D...`):

| Chave | Valores | Padrão | Padrão GoF afetado |
|---|---|---|---|
| `biblioteca.perfil` | `teste` / `producao` | `teste` | Abstract Factory |
| `biblioteca.banco` | `A` / `B` | `A` | Adapter |
| `biblioteca.politicaMulta` | `ORIGINAL` / `TETO` / `TOLERANCIA` | `ORIGINAL` | Strategy |
| `biblioteca.consulta.cache` | `true` / `false` | `true` | Decorator |
| `biblioteca.consulta.medicao` | `true` / `false` | `true` | Decorator |

---

## 2. Os dez padrões (resumo)

| # | Situação | Padrão | Onde está (pacote) | Teste do cenário |
|---|---|---|---|---|
| 1 | Ciclo de vida do livro | **State** | `dominio.livro` | `R01_CicloDeVidaDoLivroStateTest` |
| 2 | Políticas de multa | **Strategy** | `financeiro.politica` | `R02_PoliticasDeMultaStrategyTest` |
| 3 | Bancos incompatíveis | **Adapter** | `financeiro.cobranca` + `banco` | `R03_BancosIncompativeisAdapterTest` |
| 4 | Entrada única da circulação | **Facade** | `circulacao` | `R04_FachadaDeCirculacaoFacadeTest` |
| 5 | Reações à devolução | **Observer** | `devolucao` | `R05_ReacoesADevolucaoObserverTest` |
| 6 | Solicitações como objetos | **Command** | `circulacao.solicitacao` | `R06_SolicitacoesComoObjetosCommandTest` |
| 7 | Rotina comum de cadastro | **Template Method** | `cadastro` | `R07_RotinaDeCadastroTemplateMethodTest` |
| 8 | Famílias de acesso a dados | **Abstract Factory** | `persistencia` | `R08_FamiliasDeAcessoAosDadosAbstractFactoryTest` |
| 9 | Consulta com recursos opcionais | **Decorator** | `consulta` | `R09_ConsultaComRecursosOpcionaisDecoratorTest` |
| 10 | Organização hierárquica do acervo | **Composite** | `organizacao` | `R10_OrganizacaoHierarquicaCompositeTest` |

O teste `FluxoCompletoPelaInterfaceTest` executa o `roteiro-demo.txt` pela interface: reserva → empréstimo →
renovação → devolução atrasada → multa → pagamento.

Documentação detalhada:
* [docs/RASTREABILIDADE.md](docs/RASTREABILIDADE.md) — requisito → padrão → classe de cada papel → teste.
* [docs/MAPA_DO_CODIGO.md](docs/MAPA_DO_CODIGO.md) — onde encontrar cada coisa no código (arquivo e linha).
* [docs/GUIA_APRESENTACAO.md](docs/GUIA_APRESENTACAO.md) — roteiro dos 5 minutos, perguntas da arguição e alterações ao vivo.
* [docs/MODELOS_UML.md](docs/MODELOS_UML.md) — diagramas (classes por padrão, máquina de estados, sequências) e como atualizar o Astah.
* [REGISTRO_LLM.md](REGISTRO_LLM.md) — registro do uso de IA generativa.

---

## 3. Arquitetura

```
Interface (interfacecli.TelaTerminal)  ── só chama ──▶  Tratadores (tratadores.*)
                                                         │  TratadorDeEmprestimo ──▶ ExecutorDeSolicitacoes (Command)
                                                         │  TratadorDeDevolucao  ─┐
                                                         ▼                        ▼
                                              FachadaCirculacao (Facade) ── coordena ──▶ Relacionamento, Movimentacao, Acervo, Livro (State)
                                                         │ após commit
                                                         ▼
                                              PublicadorDevolucao (Observer) ─▶ inspeção, multa, indicadores, e-mail
                                                                                      │
                                                              ServicoDeMultas (Strategy) ─▶ GatewayCobranca (Adapter) ─▶ Banco A / Banco B
Acervo / Movimentacao / Relacionamento ─▶ Repositórios + UnidadeDeTrabalho criados pela FabricaDeDados (Abstract Factory)
TratadorDeAcervo ─▶ CadastroTemplate (Template Method), ConsultaAcervo decorada (Decorator), OrganizacaoDoAcervo (Composite)
```

* **Componentes do Trabalho 02 preservados:** Acadêmico (`iEstudante`), RH (`iPessoa`, `iAtendente`), Biblioteca
  (`iLivro`), Financeiro (`iMulta` → `ServicoDeMultas.emitirMulta`, `iQuitacao` → `ServicoDeMultas.registrarPagamento`) e
  Banco externo (`iCobranca` → `GatewayCobranca`).
* **Pontos internos de acesso:** `acesso.Acervo`, `acesso.Movimentacao`, `acesso.Relacionamento`.
* **Tratadores:** `TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente`,
  `TratadorDeEmprestimo`, `TratadorDeDevolucao`, `TratadorFinanceiro`, `TratadorDeNotificacoes`.
* **Raiz de composição:** `app.Sistema` é o único lugar que conhece as classes concretas (fábrica, adaptador, política,
  decoradores, ouvintes).
* **Regras do domínio preservadas:** guarda `numRenovacoes < 2`, +30 dias por renovação, prazo inicial de 14 dias,
  laudo irrecuperável leva REPARANDO → INCINERADO, livro DEVOLVIDO não pode ser reservado antes da inspeção.
* **Falhas:** regras violadas lançam `RegraDeNegocioException` (a interface mostra "RECUSADO: ..."); falhas de
  infraestrutura viram `PersistenciaException`; toda operação da fachada e todo cadastro rodam em unidade de trabalho
  (rollback em qualquer exceção).

### Estrutura de pastas
```
biblioteca-gof/
├── pom.xml, mvnw, mvnw.cmd, .mvn/          build (Maven Wrapper)
├── roteiro-demo.txt                         roteiro da demonstração
├── README.md, REGISTRO_LLM.md
├── docs/                                    rastreabilidade, mapa do código, guia, UML (.puml + .asta)
└── src/
    ├── main/java/br/pucpr/biblioteca/
    │   ├── app/            Main, Sistema (montagem), configuração, relógio ajustável, dados de exemplo
    │   ├── interfacecli/   TelaTerminal (interface de linha de comando)
    │   ├── tratadores/     tratadores (único ponto de contato da interface)
    │   ├── componentes/    interfaces iLivro, iPessoa, iEstudante, iAtendente
    │   ├── circulacao/     FachadaCirculacao, Comprovante  | solicitacao/ (Command)
    │   ├── dominio/        livro/ (State), movimentacao/, pessoa/
    │   ├── acesso/         Acervo, Movimentacao, Relacionamento
    │   ├── persistencia/   Abstract Factory  | memoria/ | relacional/ (MySQL/JDBC)
    │   ├── cadastro/       Template Method
    │   ├── devolucao/      Observer
    │   ├── financeiro/     multas | politica/ (Strategy) | cobranca/ (Adapter)
    │   ├── banco/          APIs simuladas dos bancos A e B (sistemas externos)
    │   ├── consulta/       Decorator
    │   ├── organizacao/    Composite
    │   └── notificacao/    e-mail (simulado)
    └── test/java/br/pucpr/biblioteca/cenarios/   R01..R10 + fluxo completo
```

### Simplificações assumidas
* Bancos A e B são **simulados** (`banco.BancoAApi`, `banco.BancoBApi`), com APIs propositalmente incompatíveis; o que
  importa é a conversão feita pelos adaptadores (permitido pelo enunciado).
* E-mail é simulado por uma caixa de saída em memória (`CaixaDeSaidaEmail`).
* O registro de multas do Financeiro e a organização hierárquica (Composite) ficam em memória; a persistência pelas
  duas famílias cobre Livro, Movimentação e Relacionamento, como pede o requisito 8.
* O controle de idempotência do Command é em memória (em produção seria uma tabela com chave única no id da solicitação).

---

## 4. Uso de IA generativa (LLM)

| Ferramenta / modelo | Onde foi usado |
|---|---|
| **Claude (Anthropic), modelo Claude Opus 5.5, via Claude Code (aplicativo desktop)** | Leitura dos modelos Astah do Trabalho 02 (via Astah API), proposta de arquitetura, geração do código-fonte de todos os pacotes, dos testes R01–R10, do roteiro de demonstração e da documentação (README, rastreabilidade, mapa do código, guia de apresentação, diagramas PlantUML). |

Todo o código foi revisado pela equipe; os detalhes do que foi pedido, aceito, corrigido ou descartado estão em
[REGISTRO_LLM.md](REGISTRO_LLM.md). Conforme o enunciado, a equipe é responsável por todo o código e deve ser capaz
de explicar qualquer parte dele.
