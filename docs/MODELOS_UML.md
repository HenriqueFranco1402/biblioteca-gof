# Modelos UML

## O que há nesta pasta

| Arquivo | Conteúdo |
|---|---|
| `modelos/Diagrama_classe_projeto.asta` | Diagrama de classes do Trabalho 02 (original da equipe) |
| `modelos/Projeto-2-modelagem.asta` | Casos de uso, atividades, estados, sequências, componentes e implantação do Trabalho 02 (original) |
| `uml/maquina_de_estados_livro.puml` | Máquina de estados do Livro **alinhada ao código** (eventos, guardas e ações) |
| `uml/classes_por_padrao.puml` | Um diagrama de classes para cada um dos 10 padrões, com os papéis em estereótipos |
| `uml/sequencias.puml` | Sequências: efetivar empréstimo, renovar (Command) e registrar devolução (Observer) |

Os `.puml` são texto (PlantUML). Para vê-los: extensão **PlantUML** do VS Code / IntelliJ, ou
`java -jar plantuml.jar -tpng docs/uml/*.puml`, ou colar em <https://www.plantuml.com/plantuml>.

## ⚠️ Pendente para a entrega: atualizar o Astah e exportar em PDF

O enunciado pede "Modelos UML atualizados no Astah (.asta) e exportados em PDF: diagrama de classes de cada padrão,
a máquina de estados de Livro e os diagramas de sequência dos cenários". Os `.asta` originais foram lidos (via
Astah API) para alinhar o código, mas **não foram editados**. A equipe precisa, no Astah:

1. **Máquina de estados do Livro** (`Projeto-2-modelagem.asta` › "Diagrama de estado do livro"): hoje as transições
   não têm rótulo. Colocar em cada seta o evento/guarda/ação conforme `uml/maquina_de_estados_livro.puml`:
   `reservar`, `enviarParaReparo`, `emprestar`, `renovar [numRenovacoes < 2] / vencimento += 30`, `devolver`,
   `aprovarInspecao [bom estado]`, `reprovarInspecao [danificado]`, `concluirReparo`, `registrarLaudoIrrecuperavel`.
2. **Diagrama de classes**: criar um diagrama por padrão (10) copiando as classes de `uml/classes_por_padrao.puml`.
   Ajustes em relação ao diagrama antigo:
   * `Livro.estado : String` passa a ser uma associação com `EstadoLivro` (o nome continua gravado como texto na
     persistência).
   * `Livro.atualizarEstado(String)` foi substituído pelos eventos (`reservar()`, `emprestar()`, ...).
   * `TratadorDeCadastro` foi dividido em `TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante` e
     `TratadorDeAtendente` (como no diagrama de componentes), que delegam a `CadastroTemplate`.
   * Novas classes: `FachadaCirculacao`, `SolicitacaoCirculacao`/`ExecutorDeSolicitacoes`, `PublicadorDevolucao` e
     ouvintes, `ServicoDeMultas`/`PoliticaMulta`, `GatewayCobranca`/adaptadores, `FabricaDeDados` e repositórios,
     `ConsultaAcervo` e decoradores, `NoDoAcervo`/`GrupoDoAcervo`/`ItemDoAcervo`.
3. **Sequências**: atualizar "Emprestar Livro", "renovarEmprestimoLivro" e "registrarDevolucaoLivro" conforme
   `uml/sequencias.puml` (agora passam pela `FachadaCirculacao`; a renovação passa pelo `ExecutorDeSolicitacoes`; a
   devolução publica o evento para os ouvintes depois do commit).
4. Exportar cada diagrama: no Astah, *Arquivo › Exportar imagem* (ou imprimir em PDF) e juntar em
   `docs/modelos/Modelos_UML.pdf`.
