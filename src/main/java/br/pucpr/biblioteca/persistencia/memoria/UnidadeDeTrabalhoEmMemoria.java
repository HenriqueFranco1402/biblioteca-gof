package br.pucpr.biblioteca.persistencia.memoria;

import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

/** PADRAO ABSTRACT FACTORY - Produto concreto (familia memoria): transacao por copia e restauracao. */
public final class UnidadeDeTrabalhoEmMemoria implements UnidadeDeTrabalho {

    private final BaseEmMemoria base;
    private BaseEmMemoria.Copia copiaAntesDaTransacao;

    UnidadeDeTrabalhoEmMemoria(BaseEmMemoria base) {
        this.base = base;
    }

    BaseEmMemoria base() {
        return base;
    }

    @Override
    public void iniciar() {
        if (copiaAntesDaTransacao != null) {
            throw new IllegalStateException("Ja existe transacao em andamento");
        }
        copiaAntesDaTransacao = base.copiar();
    }

    @Override
    public void confirmar() {
        copiaAntesDaTransacao = null;
    }

    @Override
    public void desfazer() {
        if (copiaAntesDaTransacao != null) {
            base.restaurar(copiaAntesDaTransacao);
            copiaAntesDaTransacao = null;
        }
    }

    @Override
    public boolean emAndamento() {
        return copiaAntesDaTransacao != null;
    }
}
