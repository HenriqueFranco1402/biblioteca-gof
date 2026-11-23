package br.pucpr.biblioteca.financeiro;

import br.pucpr.biblioteca.financeiro.cobranca.GatewayCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.RegistroCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.SolicitacaoCobranca;
import br.pucpr.biblioteca.notificacao.ServicoEmail;

import java.time.LocalDate;

/**
 * Boleto da multa (diagrama de classes: criar(valorMulta, idEstudante) e enviarPorEmail(emailEstudante)).
 *
 * <p>A emissao do boleto e a mesma para qualquer politica de multa (usa so o valor ja calculado)
 * e para qualquer banco (fala apenas com o contrato {@link GatewayCobranca}).</p>
 */
public class Boleto {

    public static final int DIAS_PARA_PAGAMENTO = 10;

    private final String idCobranca;
    private final Dinheiro valor;
    private final String idEstudante;
    private final LocalDate vencimento;
    private final RegistroCobranca registro;

    private Boleto(String idCobranca, Dinheiro valor, String idEstudante, LocalDate vencimento, RegistroCobranca registro) {
        this.idCobranca = idCobranca;
        this.valor = valor;
        this.idEstudante = idEstudante;
        this.vencimento = vencimento;
        this.registro = registro;
    }

    /** criar(valorMulta, idEstudante): registra a cobranca no banco configurado. */
    public static Boleto criar(Multa multa, String cpfPagador, LocalDate hoje, GatewayCobranca gateway) {
        LocalDate vencimento = hoje.plusDays(DIAS_PARA_PAGAMENTO);
        RegistroCobranca registro = gateway.registrar(
                new SolicitacaoCobranca(multa.getIdMulta(), cpfPagador, multa.getValor(), vencimento));
        multa.vincularReferenciaBancaria(registro.referenciaNoBanco());
        return new Boleto(multa.getIdMulta(), multa.getValor(), multa.getIdEstudante(), vencimento, registro);
    }

    /** enviarPorEmail(emailEstudante). */
    public void enviarPorEmail(String emailEstudante, ServicoEmail email) {
        email.enviar(emailEstudante, "Boleto de multa da biblioteca",
                "Valor: " + valor + " | Vencimento: " + vencimento + " | Linha digitavel: " + registro.linhaDigitavel());
    }

    public String getIdCobranca() { return idCobranca; }

    public Dinheiro getValor() { return valor; }

    public String getIdEstudante() { return idEstudante; }

    public LocalDate getVencimento() { return vencimento; }

    public RegistroCobranca getRegistro() { return registro; }
}
