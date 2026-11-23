# Guia da apresentação — Demonstração e Arguição (27/11/2026)

5 minutos por equipe, em quatro partes: **(1)** testes dos 10 cenários, **(2)** fluxo completo pela interface,
**(3)** arguição individual, **(4)** alteração ao vivo. Lembrete do critério: a nota individual é multiplicada pelo
fator de arguição (1,0 / 0,7 / 0,4 / 0) e **qualquer integrante pode ser perguntado sobre qualquer parte**.

Para achar um trecho durante a arguição, use o [MAPA_DO_CODIGO.md](MAPA_DO_CODIGO.md).

---

## 0. Checklist antes de entrar (fazer na véspera e 10 min antes)

- [ ] `java -version` mostra **17 ou superior** (se não, `$env:JAVA_HOME = "C:\...\jdk-17"`).
- [ ] Rodar uma vez `.\mvnw.cmd clean test` em casa: o wrapper baixa Maven e dependências (precisa de internet na
      primeira vez; depois fica em cache em `%USERPROFILE%\.m2`).
- [ ] IDE aberta no projeto, com estes arquivos já em abas: `Sistema.java`, `EstadoLivro.java`, `Renovacao.java`,
      `FachadaCirculacao.java`, `ExecutorDeSolicitacoes.java`, `CadastroTemplate.java`, `PublicadorDevolucao.java`.
- [ ] Dois terminais abertos na pasta do projeto: um para os testes, outro para a interface.
- [ ] Fonte do terminal grande (o professor precisa ler).
- [ ] Plano B: se algo falhar na interface, rodar `.\mvnw.cmd -q compile exec:java "-Dexec.args=roteiro-demo.txt"`
      (o mesmo fluxo, automático).

## 1. Roteiro dos 5 minutos

| Tempo | Quem | O que fazer / falar |
|---|---|---|
| 0:00–0:30 | Integrante 1 | "Implementamos o sistema do Trabalho 02 em Java 17 com Maven. A interface é de linha de comando e só chama tratadores; os tratadores delegam a uma fachada; os dez padrões estão um por pacote e cada um tem uma classe de teste R01 a R10." |
| 0:30–1:30 | Integrante 1 | Terminal 1: `.\mvnw.cmd clean test`. Enquanto roda: "Cada classe de teste tem o nome do requisito; os métodos marcados como cenário são exatamente os do enunciado." Mostrar no final: **`Tests run: 42, Failures: 0, Errors: 0` / `BUILD SUCCESS`**. |
| 1:30–4:00 | Integrante 2 | Terminal 2: `.\mvnw.cmd -q compile exec:java` e digitar o fluxo da seção 2 abaixo, comentando o padrão de cada passo. |
| 4:00–5:00 | Integrante 3 | Mostrar `Sistema.java` (montagem): "aqui a fábrica é escolhida pelo perfil, o banco pela configuração, a política pela configuração administrativa, os ouvintes são registrados e a cadeia de decoradores é montada. Por isso as extensões não mexem nas classes existentes." Ficar pronto para a alteração ao vivo. |

> Se o professor interromper com perguntas, priorizem a parte (2): o fluxo completo é obrigatório.

## 2. Fluxo completo pela interface (o que digitar e o que dizer)

Os dados de exemplo já existem: estudante **E1** (Ana), **E2** (Bruno), livros **L1..L5**.

| Comando | O que aparece | O que falar (padrão) |
|---|---|---|
| `livros` | 5 livros LIVRE | — |
| `reservar L1 E1` | `[solicitacao xxxx] Reserva ... [livro RESERVADO]` | **Command**: a tela criou um objeto `SolicitacaoReserva` com id, autor e instante, e entregou ao executor. **State**: LIVRE → RESERVADO. |
| `emprestar L1 E1` | `Emprestimo ... vence em <hoje+14> [livro EMPRESTADO]` | **Facade**: validou a reserva, removeu a reserva, criou o empréstimo e atualizou o Livro, numa transação. |
| `renovar L1 E1` | `novo vencimento ... (1/2 renovacoes)` | Guarda `numRenovacoes < 2`, +30 dias. |
| `reenviar` | mesmo comprovante, mesmo vencimento | **Command**: mesmo id → o executor não executa de novo (não soma outros 30 dias). |
| `renovar L1 E1` | `(2/2 renovacoes)` | Nova solicitação, novo id: executa. |
| `renovar L1 E1` | `RECUSADO: Renovacao recusada ... (guarda numRenovacoes < 2)` | **State**: a guarda é avaliada antes de mexer no vencimento. |
| `avancar 79` | `Hoje e ...` | Relógio da demonstração: 14 + 30 + 30 = 74 dias de prazo; 79 = 5 dias de atraso. |
| `devolver L1 E1` | `... 5 dia(s) de atraso [livro DEVOLVIDO]` | **Observer**: depois do commit, o publicador avisou inspeção, multa, indicadores e e-mail. |
| `aguardando` | `[L1]` | Ouvinte de inspeção. |
| `multas E1` | `MUL-1 R$ 5,00 (5 dia(s), politica ORIGINAL) PENDENTE` | **Strategy**: política ORIGINAL, R$ 1,00/dia; valor e política gravados na multa. **Adapter**: boleto registrado no Banco A em centavos. |
| `reservar L1 E2` | `RECUSADO: ... esta DEVOLVIDO` | Livro devolvido não inspecionado não pode ser reservado. |
| `inspecionar L1 ok` | `livro L1 agora esta LIVRE` | DEVOLVIDO → LIVRE. |
| `pagar MUL-1` | `... multa QUITADA.` | O banco simulado mandou a notificação no formato dele; o adaptador traduziu. |
| `pagar MUL-1` | `notificacao ignorada (multa ja quitada ...)` | Notificação repetida não gera segunda quitação. |
| `emails` / `indicadores` | boleto + comprovante / `devolucoes=1, atrasadas=1` | Outros ouvintes. |
| `arvore` | Campus Curitiba (5 livros) com seções | **Composite**. |

Extras se sobrar tempo: `politica TOLERANCIA` (troca a estratégia), `mover L4 Computacao` + `arvore` (Composite),
`consultar L2` duas vezes + `estatisticas` (Decorator: medição conta 2, cache acerta 1).

## 3. Arguição — perguntas prováveis e respostas

As perguntas abaixo são exatamente as listadas no enunciado ("Na arguição, saiba explicar"), mais as genéricas
"o que este trecho faz / qual papel no padrão / o que aconteceria se fosse alterado".

### R01 — State
* **Onde está a guarda da renovação e por que antes de alterar o vencimento?**
  `Renovacao.aplicar` (`dominio/livro/Renovacao.java`). O `if (numRenovacoes >= 2) throw` vem primeiro; só depois
  `atualizarDataVencimento(30)`, incremento e troca de estado. Se a ordem fosse invertida, uma renovação recusada
  já teria somado 30 dias (estado inconsistente: prazo estendido sem renovação válida).
* **O que acontece quando um evento chega a um estado que não o aceita?** A classe do estado não sobrescreve o
  método; vale a implementação padrão de `EstadoLivro`, que chama `recusar(...)` e lança
  `TransicaoInvalidaException` sem tocar no Livro. Como a fachada está numa transação, nada é gravado; a interface
  mostra "RECUSADO".
* **Como o estado é gravado e reconstituído?** Grava-se o nome (`getNomeEstado()` → "EMPRESTADO") na coluna
  `estado` (ou no registro em memória). Na leitura, `Livro.reconstituir(...)` chama `EstadoLivro.deNome("EMPRESTADO")`,
  que devolve a instância única do estado.
* **Por que os estados são instâncias únicas (`INSTANCIA`)?** Eles não guardam dados (os dados ficam no Livro);
  então um objeto por estado basta para todos os livros.
* **E se alguém colocar um `switch` no tratador?** Viola o requisito: cada nova regra exigiria mexer em vários
  lugares. Com State, a regra de cada transição está num único método do estado de origem.
* **Onde fica a ação de entrada do DEVOLVIDO (registrarDataDevolucao)?** Nos métodos `devolver` de EMPRESTADO e
  RENOVADO, antes de `mudarEstado(DEVOLVIDO)`.

### R02 — Strategy
* **Como uma nova política é incluída sem alterar o serviço?** Nova classe que implementa `PoliticaMulta` +
  `configuracaoAdministrativa.registrar(...)` na montagem. `ServicoDeMultas` só chama `politicaVigente().calcular(dias)`.
* **Por que trocar a política não recalcula multas já emitidas?** A `Multa` guarda `valor` e `politicaAplicada` em
  campos `final` no momento da emissão; o valor não é calculado de novo ao ler.
* **Como são calculados dias de atraso e valores?** `ChronoUnit.DAYS.between(vencimento, dataDevolucao)`, mínimo 0.
  Valores em centavos (`long`) na classe `Dinheiro` — evita erro de arredondamento de `double`. R$ 1,00 = 100.

### R03 — Adapter
* **Onde ficam as conversões de cada banco?** Somente em `AdaptadorBancoA` (Dinheiro → centavos, CPF → long, data →
  AAAAMMDD, "06" → PAGO) e `AdaptadorBancoB` (Dinheiro → BigDecimal "7.00", CPF → "000.000.000-00", "PAID" → PAGO).
* **Como o banco usado pela instalação é escolhido?** Chave `biblioteca.banco` (A/B) lida em `ConfiguracaoSistema`;
  `Sistema` cria o adaptador correspondente uma vez. O `ServicoDeMultas` recebe só o `GatewayCobranca`.
* **Como a quitação é protegida contra notificações repetidas?** `Multa.quitar` é `synchronized` e só muda de
  PENDENTE para QUITADA uma vez (retorna `false` depois). `registrarPagamento` só conta a quitação quando retorna
  `true`.
* **É adaptador de classe ou de objeto?** De objeto: o adaptador *contém* a API do banco (composição).

### R04 — Facade
* **Quais classes a fachada coordena e em que ordem?** (efetivar empréstimo) Relacionamento (estudante existe) →
  Movimentacao.consultarReserva (1.1) → Acervo.consultarLivro + Livro.emprestar (1.2) → Movimentacao.removerReserva
  (1.3) → criar Emprestimo (1.4) → Movimentacao.adicionar (1.5) → Acervo.atualizar.
* **O que é desfeito se um passo falhar no meio?** Tudo. A operação roda em `UnidadeDeTrabalho.executar`; qualquer
  exceção chama `desfazer()` (rollback no MySQL; restauração da cópia em memória). O teste
  `R04_falhaNoMeioDesfazTudo` simula falha no passo 1.5 e mostra que o Livro volta a RESERVADO e a reserva continua.
* **Por que a interface não acessa Movimentacao diretamente?** Para não espalhar a coordenação (e as regras) pela
  interface, não expor coleções e garantir que tudo passe por uma transação. A interface depende só dos tratadores.

### R05 — Observer
* **Como um novo ouvinte é incluído sem alterar o tratador?** Nova classe que implementa `OuvinteDevolucao` +
  `publicadorDevolucao.registrar(...)` em `Sistema`. Fachada e tratador não mudam.
* **O que acontece quando o envio de e-mail falha?** O publicador captura a exceção daquele ouvinte, registra em
  `falhas()` e continua com os outros. A devolução já foi confirmada antes da publicação, então não é desfeita
  (teste `R05_falhaDoEmailNaoAfetaDemaisNemDesfazDevolucao`).
* **Por que o evento leva os dados em vez de o ouvinte consultá-los depois?** Os dados ficam consistentes com o
  momento da devolução (outra operação poderia mudá-los), o ouvinte não precisa acessar Movimentacao/Relacionamento
  (menos acoplamento) e evita consultas repetidas.
* **Por que publicar depois do commit?** Se publicasse antes e a transação falhasse, a multa e o e-mail sairiam para
  uma devolução que não existe.

### R06 — Command
* **Quem cria, quem invoca e quem executa?** Cria: a tela (`TelaTerminal`, comandos reservar/renovar). Invoca:
  `ExecutorDeSolicitacoes` (via `TratadorDeEmprestimo.submeter`). Executa: o receptor `FachadaCirculacao` → `Livro`.
* **Como a repetição é detectada mesmo com duas submissões simultâneas?** `ConcurrentHashMap.putIfAbsent(id, tarefa)`
  é atômico: só uma thread consegue registrar a tarefa daquele id e só ela roda `run()`; as outras pegam a tarefa
  existente e esperam `get()`, recebendo o mesmo comprovante. Teste com 8 threads: `R06_submissoesSimultaneasExecutamUmaVez`.
* **Por que não há operação de desfazer?** Reserva/renovação efetivada é fato de negócio (prazo informado ao
  estudante, possíveis e-mails, outra pessoa pode ter agido depois). Corrigir é outra operação do domínio com suas
  próprias regras, não um "undo" técnico; e a regra numRenovacoes < 2 não poderia ser burlada desfazendo.

### R07 — Template Method
* **Quais passos são fixos e quais variam?** A ordem é fixa no método `final incluir`: buscar existente → validar →
  verificar pré-requisitos → criar → armazenar → apresentar. O conteúdo de cada passo varia por subclasse.
* **O que é um gancho e onde é usado?** Método com implementação padrão (vazia) que a subclasse *pode* sobrescrever:
  `verificarPreRequisitos`. Livro e Pessoa não sobrescrevem; Estudante e Atendente exigem CPF cadastrado.
* **Por que a falha do pré-requisito não permite gravação parcial?** O passo 3 vem antes de criar/armazenar (o único
  passo que grava é o 5); e o método inteiro roda numa unidade de trabalho com rollback.
* **Por que `final`?** Para nenhuma subclasse mudar a ordem (ex.: armazenar antes de validar).

### R08 — Abstract Factory
* **Onde a família é escolhida?** Uma vez, em `SeletorDeFabrica.paraPerfil(perfil, ...)`, chamado no construtor de
  `Sistema`. Perfil vem de `biblioteca.perfil` (teste/producao).
* **Como os repositórios compartilham a mesma transação?** A fábrica cria uma única unidade de trabalho e passa a
  mesma instância para os três repositórios. No JDBC, ela guarda uma única `Connection` (autocommit desligado em
  `iniciar`), então todos os comandos SQL vão para a mesma transação.
* **O que impede misturar repositório em memória com relacional?** Os construtores dos repositórios não são públicos
  (só a fábrica da família cria) e exigem a unidade de trabalho concreta da mesma família
  (`RepositorioLivroJdbc(UnidadeDeTrabalhoJdbc)`); não compila passar a de memória.
* **E o Oracle?** É réplica (replicação feita pelo banco). A `FabricaRelacional` recusa URL `jdbc:oracle`.

### R09 — Decorator
* **Como a cadeia é montada e por que a ordem importa?** Em `Sistema`: `new ConsultaComMedicao(new ConsultaComCache(
  new ConsultaAcervoOriginal(acervo)))`. Com medição por fora, todas as consultas são medidas (inclusive as do
  cache); invertendo, só as que chegam ao acervo seriam medidas (teste `R09_ordemDosDecoradoresImporta`).
* **O que acontece se o cache falhar?** `ConsultaComCache` captura a exceção e consulta o componente original; o
  usuário não percebe (teste `R09_falhaDoCacheDegradaParaOriginal`).
* **Por que o cache não participa da decisão de reserva?** Ele guarda só `MetadadosLivro` (título, autor) — sem
  estado. A reserva vai pela fachada, que lê o Livro atual no Acervo e deixa o State decidir.

### R10 — Composite
* **Como a contagem percorre a árvore?** `GrupoDoAcervo.contarLivros` soma `filho.contarLivros()` de cada filho;
  o item devolve 1; é recursivo (profundidade variável).
* **Quais regras impedem ciclos e livros em dois grupos?** Ciclo: `adicionar(subgrupo)` recusa o próprio grupo, um
  ancestral (`ehDescendenteDe`) ou grupo que já tem pai. Livro duplicado: `OrganizacaoDoAcervo` mantém um índice
  único idLivro → grupo para toda a organização.
* **Por que mover um item não toca no Livro nem na Movimentação?** O item só guarda o *id* do livro; mover é religar
  o mesmo objeto item a outro grupo e atualizar o índice. Estado e empréstimo continuam onde estão.

### Perguntas gerais
* **Onde ficam as regras do domínio?** No domínio: State (Livro/estados), Movimentacao (reserva do estudante),
  cadastros (pré-requisitos). Interface e tratadores não têm regras.
* **O que é o `Sistema`?** A raiz de composição: único lugar com `new` das classes concretas.
* **Como os testes rodam sem banco?** Perfil TESTE → família em memória; a família relacional é testada com H2 em
  modo MySQL (`R08_familiaRelacionalExecutaFluxoComTransacaoCompartilhada`).

## 4. Alteração ao vivo

Em todas, a frase-chave é: **"criamos uma classe nova que implementa o contrato do padrão e só registramos na
montagem (`Sistema`); nenhuma classe existente do serviço/tratador/fachada é alterada."** Depois, rodar
`.\mvnw.cmd -q compile exec:java` e mostrar funcionando.

### 4.1 Nova política de multa (Strategy)
Criar `src/main/java/br/pucpr/biblioteca/financeiro/politica/PoliticaFimDeSemestre.java`:
```java
package br.pucpr.biblioteca.financeiro.politica;

import br.pucpr.biblioteca.financeiro.Dinheiro;

/** R$ 2,00 por dia de atraso. */
public class PoliticaFimDeSemestre implements PoliticaMulta {
    public String nome() { return "FIM_SEMESTRE"; }

    public Dinheiro calcular(long diasDeAtraso) {
        return Dinheiro.reais(2).vezes(Math.max(0, diasDeAtraso));
    }
}
```
Registrar em `Sistema.java`, logo após `this.configuracaoAdministrativa = new ConfiguracaoAdministrativa();`
(e acrescentar `import br.pucpr.biblioteca.financeiro.politica.PoliticaFimDeSemestre;` no topo — a IDE faz com Alt+Enter):
```java
configuracaoAdministrativa.registrar(new PoliticaFimDeSemestre());
```
Demonstrar: `politica FIM_SEMESTRE`, depois o fluxo de devolução atrasada → `multas E1` mostra R$ 10,00 para 5 dias.
**Por que funciona:** `ServicoDeMultas` depende só de `PoliticaMulta`; multas antigas guardam o valor delas.

### 4.2 Novo ouvinte da devolução (Observer)
Criar `src/main/java/br/pucpr/biblioteca/devolucao/OuvinteAuditoria.java`:
```java
package br.pucpr.biblioteca.devolucao;

/** Registra cada devolucao no console (auditoria). */
public class OuvinteAuditoria implements OuvinteDevolucao {
    public String nome() { return "auditoria"; }

    public void aoDevolver(EventoDevolucao e) {
        System.out.println("[AUDITORIA] livro " + e.idLivro() + " devolvido por " + e.idEstudante()
                + " com " + e.diasDeAtraso() + " dia(s) de atraso");
    }
}
```
Registrar em `Sistema.java`, junto dos outros `publicadorDevolucao.registrar(...)`
(com `import br.pucpr.biblioteca.devolucao.OuvinteAuditoria;`):
```java
publicadorDevolucao.registrar(new OuvinteAuditoria());
```
**Por que funciona:** a fachada só chama `publicador.publicar(evento)`; ela não conhece os ouvintes.

### 4.3 Outras extensões possíveis
* **Novo decorador** (ex.: log): `class ConsultaComLog extends ConsultaAcervoDecorador`, sobrescrever
  `consultarMetadados` (imprime e chama `super`), e embrulhar em `Sistema`: `consulta = new ConsultaComLog(consulta);`.
* **Novo banco C:** `class AdaptadorBancoC implements GatewayCobranca` + `case "C" -> new AdaptadorBancoC(...)` em
  `Sistema`.
* **Novo estado/transição:** nova classe estendendo `EstadoLivro` sobrescrevendo só os eventos válidos, incluir no
  `Registro` de `EstadoLivro.deNome`, e o evento correspondente em `Livro` + `EstadoLivro` (padrão = recusar).

## 5. Divisão sugerida de estudo (mas todos devem saber tudo)

Cada integrante estuda a fundo 3–4 padrões e depois **explica para os outros**; na véspera, façam uma rodada em que
um aponta um trecho aleatório do código e outro explica (simulação da arguição).

| Integrante | Foco inicial |
|---|---|
| Henrique | State, Command, Facade (núcleo da circulação) |
| Rafael | Strategy, Adapter, Observer (devolução e financeiro) |
| Santiago | Template Method, Abstract Factory, Decorator, Composite |
