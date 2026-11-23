package br.pucpr.biblioteca.financeiro.cobranca;

import java.util.function.Consumer;

/**
 * PADRAO ADAPTER - papel: Alvo (Target). Contrato institucional de cobranca (interface iCobranca
 * do diagrama de componentes).
 *
 * <p>O Financeiro so conhece este contrato. Cada banco tem um adaptador que converte valores,
 * documentos, identificadores e situacoes para a API proprietaria do banco, e que traduz de volta
 * as notificacoes de pagamento.</p>
 */
public interface GatewayCobranca {

    /** Registra a cobranca no banco e devolve a referencia/linha digitavel. */
    RegistroCobranca registrar(SolicitacaoCobranca solicitacao);

    /** Recebe as notificacoes de pagamento JA TRADUZIDAS para o formato institucional. */
    void aoReceberPagamento(Consumer<NotificacaoPagamento> receptor);

    /** Identificacao do banco usado pela instalacao. */
    String banco();
}
