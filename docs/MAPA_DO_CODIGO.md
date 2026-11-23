# Mapa do código — onde encontrar cada coisa

Guia rápido para a **demonstração** e a **arguição**: quando o professor apontar um trecho ou perguntar "onde está X",
use as tabelas abaixo. `M/` = `src/main/java/br/pucpr/biblioteca/` · `T/` = `src/test/java/br/pucpr/biblioteca/cenarios/`.
Os números de linha valem para a versão entregue; se alguém editar o arquivo, procure pelo nome do método.

> Dica para a arguição: todo arquivo de padrão começa com um comentário `PADRAO <NOME> - papel: ...`. No IntelliJ/VS
> Code, `Ctrl+Shift+F` por `PADRAO STATE` (ou outro) lista todas as classes do padrão.

---

## 0. Por onde o programa começa

| O que | Onde |
|---|---|
| `main` | `M/app/Main.java` |
| Montagem de TUDO (fábrica, banco, política, ouvintes, decoradores, tratadores) | `M/app/Sistema.java` (construtor, linhas ~97–150) |
| Configuração (perfil, banco, política, cache, medição, MySQL) | `M/app/ConfiguracaoSistema.java` + `src/main/resources/biblioteca.properties` |
| Dados de exemplo (E1, E2, A1, L1..L5, árvore do acervo) | `M/app/DadosDeExemplo.java` |
| Relógio que avança (`avancar N`) | `M/app/RelogioAjustavel.java` |
| Interface (comandos `reservar`, `emprestar`, ...) | `M/interfacecli/TelaTerminal.java` → `processar(...)` (linhas 89–163) |

---

## 1. State — ciclo de vida do livro (`M/dominio/livro/`)

| Pergunta / trecho | Onde |
|---|---|
| Livro delega o evento ao estado | `Livro.java:57` (`reservar() { estado.reservar(this); }`) e linhas seguintes |
| Troca de estado + ação de entrada | `Livro.java:77` (`mudarEstado`) |
| Um método por evento; padrão = **recusar sem alterar** | `EstadoLivro.java:68-84` e `recusar(...)` em `EstadoLivro.java:86` |
| **Guarda da renovação** `numRenovacoes < 2` | `Renovacao.java:20` (o `if` vem ANTES de mexer no vencimento) |
| Ação "+30 dias" no vencimento | `Renovacao.java:25` → `Emprestimo.atualizarDataVencimento` |
| LIVRE → RESERVADO | `EstadoLivre.java:14` |
| RESERVADO → EMPRESTADO (zera renovações) | `EstadoReservado.java:14` |
| EMPRESTADO → RENOVADO / DEVOLVIDO | `EstadoEmprestado.java:18` / `:23` |
| RENOVADO → RENOVADO / DEVOLVIDO | `EstadoRenovado.java:18` / `:23` |
| DEVOLVIDO → LIVRE (inspeção ok) | `EstadoDevolvido.java:19` |
| **Laudo irrecuperável** REPARANDO → INCINERADO | `EstadoReparando.java:22` |
| INCINERADO (final, recusa tudo) | `EstadoIncinerado.java` (não sobrescreve nada) |
| **Gravação** do estado (texto) | `M/persistencia/memoria/RepositorioLivroEmMemoria.java` (`salvar`) e `M/persistencia/relacional/RepositorioLivroJdbc.java` (coluna `estado`) |
| **Reconstituição** do estado | `Livro.java:50` (`reconstituir`) → `EstadoLivro.java:51` (`deNome`) |
| Testes | `T/R01_CicloDeVidaDoLivroStateTest.java` |

**O que acontece se um evento chega a um estado que não o aceita?** Cai na implementação padrão de `EstadoLivro`,
que lança `TransicaoInvalidaException` antes de qualquer alteração; a fachada está dentro de uma transação, então
nada é gravado; a interface mostra `RECUSADO: ...`.

---

## 2. Strategy — políticas de multa (`M/financeiro/`)

| Pergunta / trecho | Onde |
|---|---|
| Interface da estratégia | `politica/PoliticaMulta.java` |
| R$ 1,00 por dia | `politica/PoliticaOriginal.java:17` |
| Teto de R$ 20,00 | `politica/PoliticaComTeto.java:17` |
| Tolerância de 2 dias | `politica/PoliticaComTolerancia.java:17` |
| Política vigente (configuração administrativa) | `politica/ConfiguracaoAdministrativa.java:31` (`definirPoliticaVigente`), registro em `:27` |
| Contexto usa a política sem saber qual é | `ServicoDeMultas.java:58` |
| Multa **grava valor e política** na emissão | `ServicoDeMultas.java:64` (`new Multa(..., valor, politica.nome(), ...)`), campos `final` em `Multa.java` |
| Cálculo dos dias de atraso | `ServicoDeMultas.diasDeAtraso` (`ChronoUnit.DAYS.between(vencimento, devolucao)`, mínimo 0) |
| Valores monetários | `Dinheiro.java` (centavos em `long`; `emReais()` → BigDecimal 2 casas) |
| Boleto não muda com a política | `Boleto.java:35` (usa só `multa.getValor()`) |
| Testes | `T/R02_PoliticasDeMultaStrategyTest.java` |

---

## 3. Adapter — bancos incompatíveis (`M/financeiro/cobranca/` e `M/banco/`)

| Pergunta / trecho | Onde |
|---|---|
| Contrato institucional (`iCobranca`) | `cobranca/GatewayCobranca.java` |
| **Conversões do Banco A** (centavos, CPF numérico, data AAAAMMDD) | `cobranca/AdaptadorBancoA.java:39-47` (`s.valor().centavos()` na linha 41) |
| **Conversões do Banco B** (BigDecimal, CPF formatado) | `cobranca/AdaptadorBancoB.java:34-42` (`emReais()` linha 36, `formatarCpf` linha 37) |
| Tradução da notificação de pagamento A ("protocolo;06;data") | `AdaptadorBancoA.java:60` (`traduzirRetorno`), código "06" → PAGO na linha 67 |
| Tradução da notificação do B (evento "PAID") | `AdaptadorBancoB.java:55` (`traduzirEvento`), linha 61 |
| APIs simuladas (não mexer: são "externas") | `M/banco/BancoAApi.java:32`, `M/banco/BancoBApi.java:36` |
| **Como o banco da instalação é escolhido** | `M/app/Sistema.java:110-113` (`switch (configuracao.banco())`), chave `biblioteca.banco` |
| Serviço recebe notificações já traduzidas | `ServicoDeMultas.java:44` (`gateway.aoReceberPagamento(this::registrarPagamento)`) |
| **Proteção contra notificação repetida** | `ServicoDeMultas.java:84` (`registrarPagamento`) e `Multa.java:37` (`quitar` sincronizado, retorna `false` se já QUITADA) |
| Testes | `T/R03_BancosIncompativeisAdapterTest.java` |

---

## 4. Facade — fachada de circulação (`M/circulacao/FachadaCirculacao.java`)

| Pergunta / trecho | Onde |
|---|---|
| Reservar | `FachadaCirculacao.java:58` |
| **Efetivar empréstimo** (ordem do diagrama de sequência) | `:74` — 1.1 `consultarReserva` (`:77`), 1.2 `livro.emprestar()` (`:79`), 1.3 `removerReserva` (`:80`), 1.4 `Emprestimo.novo` (`:81`), 1.5 `adicionar` (`:82`) |
| Renovar | `:93` (`livro.renovar(emprestimo)` em `:97`) |
| Registrar devolução (commit e depois publicação) | `:115`, commit em `:134`, publicação em `:136` |
| Inspeção / reparo / laudo | `:144`, `:159`, `:163`, `:168` |
| **O que é desfeito se um passo falhar** | Tudo: cada método roda em `unidadeDeTrabalho.executar(...)` — `M/persistencia/UnidadeDeTrabalho.java:26` (rollback no `catch`) |
| Comprovantes em vez de coleções | `M/circulacao/Comprovante.java` |
| Tratadores que delegam à fachada | `M/tratadores/TratadorDeEmprestimo.java`, `M/tratadores/TratadorDeDevolucao.java` |
| Testes | `T/R04_FachadaDeCirculacaoFacadeTest.java` (`R04_falhaNoMeioDesfazTudo` simula falha no passo 1.5) |

**Por que a interface não acessa Movimentacao?** A `TelaTerminal` só recebe tratadores (veja os campos no topo da
classe); Movimentacao só é criada dentro de `Sistema` e passada à fachada. Se a interface mexesse direto, poderia
remover a reserva sem atualizar o Livro (estado inconsistente) e fora de transação.

---

## 5. Observer — reações à devolução (`M/devolucao/`)

| Pergunta / trecho | Onde |
|---|---|
| Publicador (lista de ouvintes, notifica isolando falhas) | `PublicadorDevolucao.java:31` (`publicar`), `catch` em `:35` |
| Interface do ouvinte | `OuvinteDevolucao.java` |
| Ouvintes concretos | `OuvinteInspecao.java:18`, `OuvinteMulta.java:24`, `OuvinteIndicadores.java:18`, `OuvinteEmail.java:24` |
| Evento com todos os dados | `EventoDevolucao.java` |
| Publicação **após** confirmar | `M/circulacao/FachadaCirculacao.java:134-136` |
| **Onde os ouvintes são registrados** (incluir um novo) | `M/app/Sistema.java:121-124` |
| Falha de e-mail | O publicador captura, registra em `falhas()` e segue para o próximo ouvinte |
| Testes | `T/R05_ReacoesADevolucaoObserverTest.java` |

---

## 6. Command — solicitações (`M/circulacao/solicitacao/`)

| Pergunta / trecho | Onde |
|---|---|
| Comando (id, autor, instante, `executar`) | `SolicitacaoCirculacao.java:38` |
| Reserva / Renovação | `SolicitacaoReserva.java:21`, `SolicitacaoRenovacao.java:21` |
| **Quem cria** | `M/interfacecli/TelaTerminal.java:117` e `:119` |
| **Quem invoca** | `ExecutorDeSolicitacoes.java:28` |
| **Quem executa** | `FachadaCirculacao` + `Livro` (receptores) |
| **Detecção de repetição, inclusive simultânea** | `ExecutorDeSolicitacoes.java:30` (`putIfAbsent` atômico), `:33` (só o vencedor executa), `:36` (os outros recebem o mesmo resultado) |
| Demonstrar repetição na interface | comando `reenviar` (`TelaTerminal.java:121`) |
| Testes | `T/R06_SolicitacoesComoObjetosCommandTest.java` |

---

## 7. Template Method — cadastros (`M/cadastro/`)

| Pergunta / trecho | Onde |
|---|---|
| **Método-modelo `final`** | `CadastroTemplate.java:36` |
| Ordem fixa dos passos | `CadastroTemplate.java:38-46` (buscar → validar → pré-requisitos → criar → armazenar → apresentar) |
| **Gancho** (padrão vazio) | `CadastroTemplate.java:57` (`verificarPreRequisitos`) |
| Gancho sobrescrito — CPF precisa existir | `CadastroDeEstudante.java:36`, `CadastroDeAtendente.java:35` |
| Livro e Pessoa (não usam o gancho) | `CadastroDeLivro.java`, `CadastroDePessoa.java` |
| Interfaces preservadas | `M/componentes/iLivro.java`, `iPessoa.java`, `iEstudante.java`, `iAtendente.java` → implementadas em `M/tratadores/` |
| Testes | `T/R07_RotinaDeCadastroTemplateMethodTest.java` |

---

## 8. Abstract Factory — persistência (`M/persistencia/`)

| Pergunta / trecho | Onde |
|---|---|
| Fábrica abstrata | `FabricaDeDados.java` |
| **Onde a família é escolhida** (uma única vez) | `SeletorDeFabrica.java:16-19`, chamado em `M/app/Sistema.java:94` |
| Fábrica em memória | `memoria/FabricaEmMemoria.java` |
| Fábrica relacional (MySQL) | `relacional/FabricaRelacional.java` (cria UMA unidade de trabalho e passa aos 3 repositórios, linhas 47–50) |
| **Repositórios compartilham a mesma transação** | `relacional/UnidadeDeTrabalhoJdbc.java:29` (uma `Connection`), `:48` (`setAutoCommit(false)`), `:71` (`rollback`) |
| Transação em memória | `memoria/UnidadeDeTrabalhoEmMemoria.java:24` (cópia) e `:35` (restaura) |
| **O que impede misturar famílias** | Construtores dos repositórios são de pacote e exigem a unidade de trabalho concreta da própria família (ex.: `RepositorioLivroJdbc(UnidadeDeTrabalhoJdbc)`) |
| Oracle só réplica | `relacional/FabricaRelacional.java:43` |
| Criação das tabelas | `relacional/FabricaRelacional.java:55` (`inicializar`) |
| Testes | `T/R08_FamiliasDeAcessoAosDadosAbstractFactoryTest.java` |

---

## 9. Decorator — consulta (`M/consulta/`)

| Pergunta / trecho | Onde |
|---|---|
| Contrato | `ConsultaAcervo.java` |
| Original (sem alterações) | `ConsultaAcervoOriginal.java:17` |
| Decorador abstrato | `ConsultaAcervoDecorador.java` |
| Medição | `ConsultaComMedicao.java:22` (conta em `:27`) |
| Cache (só metadados) | `ConsultaComCache.java:29`, leitura do cache em `:31` |
| **Se o cache falhar** | `ConsultaComCache.java:38` (degrada para a consulta original) |
| **Montagem da cadeia** (e por que a ordem importa) | `M/app/Sistema.java:131-136` → `medicao(cache(original))` |
| **Por que o cache não decide reserva** | A reserva vai pela fachada (`FachadaCirculacao.java:58`) que lê o Livro no Acervo; o cache guarda `MetadadosLivro` (sem estado) |
| Testes | `T/R09_ConsultaComRecursosOpcionaisDecoratorTest.java` |

---

## 10. Composite — organização do acervo (`M/organizacao/`)

| Pergunta / trecho | Onde |
|---|---|
| Componente | `NoDoAcervo.java` |
| Folha (referencia o livro pelo id) | `ItemDoAcervo.java` (`contarLivros` devolve 1, linha 23) |
| Composto | `GrupoDoAcervo.java` |
| **Como a contagem percorre a árvore** | `GrupoDoAcervo.java:91-94` (soma recursiva dos filhos) |
| **Regra contra ciclos** | `GrupoDoAcervo.java:40` (`ehDescendenteDe`) |
| **Regra "livro em um só grupo"** | `GrupoDoAcervo.java:55` → `OrganizacaoDoAcervo.java:33` (`reservarLugar`, índice global) |
| **Mover** sem tocar Livro/Movimentacao | `OrganizacaoDoAcervo.java:45` (só religa o mesmo item, `:54`) |
| Testes | `T/R10_OrganizacaoHierarquicaCompositeTest.java` |

---

## 11. Componentes e tratadores do Trabalho 02

| Componente (diagrama de componentes) | Interface | Implementação |
|---|---|---|
| Biblioteca | `iLivro` | `M/tratadores/TratadorDeAcervo.java` |
| RH | `iPessoa`, `iAtendente` | `M/tratadores/TratadorDePessoa.java`, `TratadorDeAtendente.java` |
| Acadêmico | `iEstudante` | `M/tratadores/TratadorDeEstudante.java` |
| Financeiro | `iMulta`, `iQuitacao` | `M/financeiro/ServicoDeMultas.java` (`emitirMulta`, `registrarPagamento`) |
| Banco (externo) | `iCobranca` | `M/financeiro/cobranca/GatewayCobranca.java` + adaptadores |

Pontos internos de acesso: `M/acesso/Acervo.java`, `M/acesso/Movimentacao.java`, `M/acesso/Relacionamento.java`.

---

## 12. Onde mexer em cada "alteração ao vivo" provável

| Pedido do professor | Arquivo novo | Única linha a acrescentar em código existente |
|---|---|---|
| Nova política de multa | `M/financeiro/politica/PoliticaXxx.java` implementando `PoliticaMulta` | `M/financeiro/politica/ConfiguracaoAdministrativa.java` (construtor) **ou** `Sistema` (`configuracaoAdministrativa.registrar(new PoliticaXxx())`) |
| Novo ouvinte da devolução | `M/devolucao/OuvinteXxx.java` implementando `OuvinteDevolucao` | `M/app/Sistema.java:124` (`publicadorDevolucao.registrar(new OuvinteXxx(...))`) |
| Novo decorador da consulta | `M/consulta/ConsultaComXxx.java` estendendo `ConsultaAcervoDecorador` | `M/app/Sistema.java:131-136` |
| Novo banco C | `M/financeiro/cobranca/AdaptadorBancoC.java` implementando `GatewayCobranca` | `M/app/Sistema.java:110-113` (`case "C" -> ...`) |
| Novo tipo de solicitação | `M/circulacao/solicitacao/SolicitacaoXxx.java` estendendo `SolicitacaoCirculacao` | `TelaTerminal` (novo comando que cria a solicitação) |

Passo a passo de cada uma, com código pronto, em [GUIA_APRESENTACAO.md](GUIA_APRESENTACAO.md#4-alteração-ao-vivo).
