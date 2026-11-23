# Tabela de rastreabilidade

Para cada requisito: o padrão, as classes que desempenham cada papel e o(s) teste(s) que comprovam o cenário.
Caminhos relativos a `src/main/java/br/pucpr/biblioteca/` (código) e `src/test/java/br/pucpr/biblioteca/cenarios/` (testes).

## R01 — Ciclo de vida do livro — **State**

| Papel | Classe |
|---|---|
| Contexto | `dominio/livro/Livro` (delega cada evento ao estado atual) |
| Estado abstrato | `dominio/livro/EstadoLivro` (um método por evento; padrão = recusar sem alterar) |
| Estados concretos (7) | `EstadoLivre`, `EstadoReservado`, `EstadoEmprestado`, `EstadoRenovado`, `EstadoDevolvido`, `EstadoReparando`, `EstadoIncinerado` |
| Corpo da transição "renovar" (guarda + ação) | `dominio/livro/Renovacao` (usado por EMPRESTADO e RENOVADO) |
| Gravação/reconstituição | `Livro.getNomeEstado()` / `Livro.reconstituir(...)` / `EstadoLivro.deNome(...)`; repositórios `RepositorioLivroEmMemoria`, `RepositorioLivroJdbc` (coluna `estado`) |

| Transição do diagrama | Método (classe do estado de origem) |
|---|---|
| LIVRE → RESERVADO | `EstadoLivre.reservar` |
| LIVRE → REPARANDO | `EstadoLivre.enviarParaReparo` |
| RESERVADO → EMPRESTADO | `EstadoReservado.emprestar` |
| EMPRESTADO → RENOVADO [numRenovacoes < 2] | `EstadoEmprestado.renovar` → `Renovacao.aplicar` |
| RENOVADO → RENOVADO [numRenovacoes < 2] | `EstadoRenovado.renovar` → `Renovacao.aplicar` |
| EMPRESTADO → DEVOLVIDO | `EstadoEmprestado.devolver` (entry / registrarDataDevolucao) |
| RENOVADO → DEVOLVIDO | `EstadoRenovado.devolver` |
| DEVOLVIDO → LIVRE (bom estado) | `EstadoDevolvido.aprovarInspecao` |
| DEVOLVIDO → REPARANDO (danificado) | `EstadoDevolvido.reprovarInspecao` |
| REPARANDO → LIVRE | `EstadoReparando.concluirReparo` |
| REPARANDO → INCINERADO (laudo irrecuperável) | `EstadoReparando.registrarLaudoIrrecuperavel` |

**Testes:** `R01_CicloDeVidaDoLivroStateTest`
* `R01_terceiraRenovacaoRecusada` — **cenário**: 3ª renovação recusada, vencimento inalterado.
* `R01_livroDevolvidoNaoInspecionadoNaoPodeSerReservado` — **cenário**.
* `R01_laudoIrrecuperavelLevaReparandoAIncinerado`, `R01_eventoInvalidoNaoAlteraOLivro`,
  `R01_transicoesDoDiagramaDeEstados`, `R01_estadoGravadoEReconstituidoNaPersistencia`.

## R02 — Políticas de cálculo de multa — **Strategy**

| Papel | Classe |
|---|---|
| Estratégia | `financeiro/politica/PoliticaMulta` |
| Estratégias concretas | `PoliticaOriginal` (R$ 1,00/dia), `PoliticaComTeto` (teto R$ 20,00), `PoliticaComTolerancia` (2 dias) |
| Contexto | `financeiro/ServicoDeMultas` (`emitirMulta`) |
| Seleção da política vigente | `financeiro/politica/ConfiguracaoAdministrativa` (registro por nome + vigente) |
| Valor/política congelados na emissão | `financeiro/Multa` (`valor`, `politicaAplicada` finais) |
| Emissão de boleto (não muda) | `financeiro/Boleto.criar` |
| Valores monetários | `financeiro/Dinheiro` (centavos em `long`) |

**Testes:** `R02_PoliticasDeMultaStrategyTest`
* `R02_cincoDiasOriginalCincoReaisToleranciaTresReaisSemAtrasoZero` — **cenário**.
* `R02_multaGravaValorEPoliticaETrocaNaoRecalculaEmitidas`, `R02_novaPoliticaSemAlterarServico`.

## R03 — Integração com bancos incompatíveis — **Adapter**

| Papel | Classe |
|---|---|
| Alvo (contrato institucional `iCobranca`) | `financeiro/cobranca/GatewayCobranca` (+ `SolicitacaoCobranca`, `RegistroCobranca`, `NotificacaoPagamento`) |
| Adaptadores | `financeiro/cobranca/AdaptadorBancoA`, `financeiro/cobranca/AdaptadorBancoB` |
| Adaptados (APIs externas simuladas) | `banco/BancoAApi` (centavos, CPF numérico, retorno "protocolo;06;data"), `banco/BancoBApi` (BigDecimal, CPF formatado, UUID, evento "PAID") |
| Cliente | `financeiro/ServicoDeMultas` (sem nenhum `if` por banco) |
| Escolha do banco da instalação | `app/Sistema` (chave `biblioteca.banco`) |
| Proteção contra notificação repetida | `ServicoDeMultas.registrarPagamento` + `Multa.quitar` (sincronizado, devolve `false` se já quitada) |

**Testes:** `R03_BancosIncompativeisAdapterTest`
* `R03_seteReaisViram700CentavosNoBancoAE7ReaisNoBancoB` — **cenário**.
* `R03_servicoDeMultasIgualParaOsDoisBancos` (inclui verificação de que o fonte do serviço não cita banco concreto),
  `R03_notificacaoRepetidaNaoGeraSegundaQuitacao`, `R03_conversaoDeDocumentos`.

## R04 — Entrada única para as operações de circulação — **Facade**

| Papel | Classe |
|---|---|
| Fachada | `circulacao/FachadaCirculacao` |
| Subsistemas coordenados | `acesso/Relacionamento`, `acesso/Movimentacao`, `acesso/Acervo`, `dominio/livro/Livro` |
| Transação | `persistencia/UnidadeDeTrabalho.executar` |
| Comprovantes (em vez de coleções) | `circulacao/Comprovante` (records `Reserva`, `Emprestimo`, `Renovacao`, `Devolucao`, `SituacaoLivro`) |
| Clientes da fachada | `tratadores/TratadorDeEmprestimo`, `tratadores/TratadorDeDevolucao` (e os comandos do R06) |

**Testes:** `R04_FachadaDeCirculacaoFacadeTest`
* `R04_efetivacaoDeEmprestimoCoordenaTudoSemExporColecoes` — **cenário**.
* `R04_emprestimoSemReservaRecusado`, `R04_falhaNoMeioDesfazTudo` (falha simulada no passo 1.5 → tudo desfeito).

## R05 — Reações independentes à devolução — **Observer**

| Papel | Classe |
|---|---|
| Sujeito / Publicador | `devolucao/PublicadorDevolucao` |
| Observador | `devolucao/OuvinteDevolucao` |
| Observadores concretos | `OuvinteInspecao`, `OuvinteMulta`, `OuvinteIndicadores`, `OuvinteEmail` |
| Evento (dados completos) | `devolucao/EventoDevolucao` (vencimento, data de devolução, estudante, e-mail, CPF) |
| Publicação após confirmação | `FachadaCirculacao.registrarDevolucao` (publica depois do `executar`/commit) |
| Registro dos ouvintes | `app/Sistema` |

**Testes:** `R05_ReacoesADevolucaoObserverTest`
* `R05_devolucaoAtrasadaAlimentaInspecaoMultaEIndicadores` — **cenário** (inclui "permanece DEVOLVIDO até a inspeção").
* `R05_devolucaoNoPrazoNaoGeraMulta` — **cenário**.
* `R05_falhaDoEmailNaoAfetaDemaisNemDesfazDevolucao`, `R05_novoOuvinteSemAlterarTratador`.

## R06 — Solicitações de circulação como objetos — **Command**

| Papel | Classe |
|---|---|
| Comando | `circulacao/solicitacao/SolicitacaoCirculacao` (id, autor, instante, `executar`) |
| Comandos concretos | `SolicitacaoReserva`, `SolicitacaoRenovacao` |
| Cliente (cria) | `interfacecli/TelaTerminal` (comandos `reservar`, `renovar`, `reenviar`) |
| Invocador (garante execução única por id) | `circulacao/solicitacao/ExecutorDeSolicitacoes` (`ConcurrentHashMap.putIfAbsent` + `FutureTask`) |
| Receptor | `circulacao/FachadaCirculacao` → `Livro` (regras no domínio) |
| Encaminhamento | `tratadores/TratadorDeEmprestimo.submeter` |

**Testes:** `R06_SolicitacoesComoObjetosCommandTest`
* `R06_mesmaRenovacaoDuasVezesAcrescentaApenas30Dias` — **cenário**.
* `R06_solicitacoesDiferentesRespeitamLimiteDeDuas` — **cenário**.
* `R06_submissoesSimultaneasExecutamUmaVez` (8 threads, mesmo id).

## R07 — Rotina comum para cadastros — **Template Method**

| Papel | Classe |
|---|---|
| Classe abstrata com método-modelo `final` | `cadastro/CadastroTemplate.incluir` |
| Passos fixos (ordem) | buscar existente → validar → verificar pré-requisitos → criar → armazenar → apresentar |
| Gancho | `CadastroTemplate.verificarPreRequisitos` (padrão vazio) |
| Classes concretas | `CadastroDeLivro`, `CadastroDePessoa`, `CadastroDeEstudante` (sobrescreve o gancho), `CadastroDeAtendente` (sobrescreve o gancho) |
| Interfaces preservadas | `componentes/iLivro`, `iPessoa`, `iEstudante`, `iAtendente` (implementadas pelos tratadores) |

**Testes:** `R07_RotinaDeCadastroTemplateMethodTest`
* `R07_estudanteDuplicadoRetornaCadastroExistente` — **cenário**.
* `R07_estudanteComCpfNaoCadastradoFalhaAntesDeGravar` — **cenário** (espião prova que `criar`/`armazenar` não rodaram).
* `R07_metodoModeloEFinalEOrdemFixa`, `R07_outrosCadastrosUsamAMesmaRotina`.

## R08 — Famílias compatíveis de acesso aos dados — **Abstract Factory**

| Papel | Classe |
|---|---|
| Fábrica abstrata | `persistencia/FabricaDeDados` |
| Fábricas concretas | `persistencia/memoria/FabricaEmMemoria`, `persistencia/relacional/FabricaRelacional` (MySQL) |
| Produtos abstratos | `UnidadeDeTrabalho`, `RepositorioLivro`, `RepositorioMovimentacao`, `RepositorioRelacionamento` |
| Produtos concretos (memória) | `UnidadeDeTrabalhoEmMemoria`, `RepositorioLivroEmMemoria`, `RepositorioMovimentacaoEmMemoria`, `RepositorioRelacionamentoEmMemoria` |
| Produtos concretos (relacional) | `UnidadeDeTrabalhoJdbc`, `RepositorioLivroJdbc`, `RepositorioMovimentacaoJdbc`, `RepositorioRelacionamentoJdbc` |
| Escolha única pelo perfil | `persistencia/SeletorDeFabrica.paraPerfil` chamado uma vez em `app/Sistema` |
| Oracle somente réplica | `FabricaRelacional` (recusa URL `jdbc:oracle`) |

**Testes:** `R08_FamiliasDeAcessoAosDadosAbstractFactoryTest`
* `R08_configuracaoDeTesteForneceApenasObjetosEmMemoria` — **cenário**.
* `R08_configuracaoDeProducaoForneceRepositoriosMySqlComMesmaUnidadeDeTrabalho` — **cenário**.
* `R08_oracleRecusadoComoBancoDeEscrita`, `R08_naoHaComoMisturarFamilias`,
  `R08_familiaRelacionalExecutaFluxoComTransacaoCompartilhada` (H2 em modo MySQL: commit visível e rollback conjunto).

## R09 — Serviço de consulta com recursos opcionais — **Decorator**

| Papel | Classe |
|---|---|
| Componente | `consulta/ConsultaAcervo` |
| Componente concreto (original, sem alterações) | `consulta/ConsultaAcervoOriginal` |
| Decorador abstrato | `consulta/ConsultaAcervoDecorador` |
| Decoradores concretos | `consulta/ConsultaComMedicao`, `consulta/ConsultaComCache` (+ `ArmazenamentoCache`) |
| Dado armazenado no cache | `consulta/MetadadosLivro` (sem estado) |
| Montagem da cadeia | `app/Sistema`: `medicao(cache(original))` conforme a instalação |
| Reserva usa o estado atual | `FachadaCirculacao.reservar` → `Acervo.consultarLivro` → `Livro.reservar` (State) |

**Testes:** `R09_ConsultaComRecursosOpcionaisDecoratorTest`
* `R09_segundaConsultaUsaCacheEGeraMedicao` — **cenário**.
* `R09_cacheAntigoNaoAutorizaReserva` — **cenário**.
* `R09_falhaDoCacheDegradaParaOriginal`, `R09_ordemDosDecoradoresImporta`.

## R10 — Organização hierárquica do acervo — **Composite**

| Papel | Classe |
|---|---|
| Componente | `organizacao/NoDoAcervo` (`contarLivros`, `listarLivros`) |
| Composto | `organizacao/GrupoDoAcervo` (UNIDADE, SECAO, SUBSECAO — `TipoGrupo`) |
| Folha | `organizacao/ItemDoAcervo` (referencia o livro pelo id) |
| Regras (ciclos, livro único) e mover | `organizacao/OrganizacaoDoAcervo` (índice idLivro → grupo), `GrupoDoAcervo.adicionar` |
| Uso pela interface | `tratadores/TratadorDeAcervo` (`criarUnidade`, `criarGrupo`, `alocarLivro`, `moverLivro`, `contarLivros`, `arvore`) |

**Testes:** `R10_OrganizacaoHierarquicaCompositeTest`
* `R10_unidadeComDuasSecoesInformaCincoLivros` — **cenário**.
* `R10_moverLivroNaoAlteraIdEstadoNemEmprestimo` — **cenário**.
* `R10_grupoRecusaCiclos`, `R10_livroEmOutroGrupoRecusado`, `R10_profundidadeVariavel`.

## Fluxo completo (demonstração)

`FluxoCompletoPelaInterfaceTest.fluxoCompletoDoRoteiroDeDemonstracao` executa `roteiro-demo.txt` pela
`TelaTerminal`: reserva, empréstimo, renovação (com reenvio do mesmo comando), 3ª renovação recusada, devolução com
5 dias de atraso, multa de R$ 5,00, reserva recusada antes da inspeção, inspeção, pagamento e notificação repetida.
