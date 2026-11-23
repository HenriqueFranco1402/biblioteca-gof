# REGISTRO_LLM — uso de IA generativa

**Ferramenta:** Claude Code (aplicativo desktop) · **Modelo:** Claude Opus 5.5 (Anthropic)
**Período:** 23/11/2026 · **Equipe responsável:** Henrique Franco Nishimura de Lima, Rafael Militão Marques,
Santiago Rodrigues da Rocha

O enunciado permite o uso livre de LLMs, desde que a equipe registre os pedidos e o que foi aceito, corrigido ou
descartado, e que todos os integrantes saibam explicar todo o código.

---

## 1. Principais pedidos feitos ao modelo

| # | Pedido | Material fornecido |
|---|---|---|
| 1 | Implementar o sistema de empréstimo com os 10 padrões GoF do enunciado, incluindo código, testes e README | PDF do enunciado; arquivos Astah `Diagrama_classe_projeto.asta` e `Projeto-2-modelagem.asta` do Trabalho 02 |
| 2 | Ler os diagramas do Astah para alinhar o código ao modelo | O modelo usou a **Astah API** (`astah-api.jar`) para listar classes, operações, máquina de estados e diagramas de sequência |
| 3 | Gerar um teste JUnit por cenário de verificação, com nome que identifique o requisito (R01..R10) | — |
| 4 | Criar o repositório Git com código-fonte, testes e documentação | — |
| 5 | Criar um guia de apresentação e um guia para encontrar cada coisa no código (demonstração e arguição) | — |

## 2. O que foi aceito

* Arquitetura em camadas: interface → tratadores → fachada → pontos de acesso (Acervo, Movimentacao,
  Relacionamento) → repositórios criados por uma fábrica abstrata.
* Nomes do Trabalho 02 preservados: `Livro`, `Reserva`, `Emprestimo`, `Devolucao`, `Transacao`, `Movimentacao`,
  `Boleto`, `TratadorDeEmprestimo`, `TratadorDeDevolucao`, interfaces `iLivro`, `iPessoa`, `iEstudante`, `iAtendente`,
  `iCobranca` (como `GatewayCobranca`), operações `atualizarDataVencimento(dias)`, `consultarReserva`,
  `removerReserva`, `consultarEmprestimo`, `consultarDevolucao`.
* Sequência "Emprestar Livro" (1.1 consultarReserva → 1.2 atualizar Livro → 1.3 removerReserva → 1.4 criar
  Empréstimo → 1.5 adicionar) reproduzida em `FachadaCirculacao.efetivarEmprestimo`.
* Máquina de estados com 7 estados, uma classe por estado e um método por transição (tabela em
  `docs/RASTREABILIDADE.md`).
* Valores monetários em centavos (`Dinheiro`), conversões de banco só nos adaptadores.
* Família relacional testada com H2 em modo MySQL (o enunciado exige que os testes rodem sem configuração
  manual).
* Interface de linha de comando com roteiro de demonstração (`roteiro-demo.txt`) e relógio ajustável
  (`avancar N`) para mostrar a devolução atrasada.

## 3. O que foi corrigido durante a geração

| Problema encontrado | Correção |
|---|---|
| Os testes falharam com `NoClassDefFoundError: EstadoLivre`: o mapa estático `nome → estado` em `EstadoLivro` era inicializado antes das subclasses (ciclo de inicialização estática) | Mapa movido para uma classe interna `Registro` (inicialização preguiçosa / *holder*) |
| `INSERT ... VALUES ((SELECT MAX(...) FROM transacao))` não funciona no MySQL (erro 1093: subconsulta na própria tabela) | A sequência passou a ser lida antes, em um `SELECT` separado na mesma transação |
| `registrarDevolucao` devolvia `Object[]` de dentro da transação | Substituído por um `record ResultadoDevolucao` |
| O comando `pagar` não mostrava se a notificação repetida foi ignorada | `TratadorFinanceiro.simularPagamentoNoBanco` passou a informar "QUITADA" ou "notificação ignorada" |
| README dizia "testado com JDK 17" sem esse teste ter sido feito | Texto corrigido para "compilado com `--release 17`, testado com JDK 26" |

## 4. O que foi descartado

* **Exportar os diagramas pelo `astah-command`**: falhou por causa da licença do Astah instalado. Os diagramas foram
  lidos pela Astah API e redesenhados em PlantUML (`docs/uml/*.puml`); a atualização do `.asta` e a exportação em PDF
  ficam a cargo da equipe (ver `docs/MODELOS_UML.md`).
* **Transição extra "cancelar reserva" (RESERVADO → LIVRE)**: não existe no diagrama de estados do Trabalho 02, então
  não foi implementada, para manter código e modelo coerentes.
* **Desfazer (undo) no Command**: descartado de propósito; o enunciado pergunta "por que não há operação de desfazer"
  (resposta no guia de apresentação).
* **Interface web**: descartada em favor da linha de comando (permitida pelo enunciado e mais fácil de demonstrar em
  5 minutos).

## 5. Revisão da equipe (preencher)

> Antes da entrega, cada integrante deve ler o código e registrar aqui o que revisou, o que alterou e as dúvidas
> resolvidas. Código que nenhum integrante consegue explicar é considerado não implementado.

| Integrante | Partes revisadas | Alterações feitas | Observações |
|---|---|---|---|
| Henrique | | | |
| Rafael | | | |
| Santiago | | | |
