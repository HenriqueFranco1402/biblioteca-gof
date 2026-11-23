package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dominio.pessoa.Estudante;
import br.pucpr.biblioteca.dominio.pessoa.Pessoa;
import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/**
 * PADRAO TEMPLATE METHOD - Classe concreta: cadastro de Estudante (diagrama "manterEstudante").
 * Sobrescreve o GANCHO {@link #verificarPreRequisitos}: o CPF precisa estar cadastrado como Pessoa.
 */
public class CadastroDeEstudante extends CadastroTemplate<Dados.Estudante, Estudante> {

    private final Relacionamento relacionamento;

    public CadastroDeEstudante(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Estudante> buscarExistente(Dados.Estudante d) {
        return d.idEstudante() == null ? Optional.empty() : relacionamento.consultarEstudante(d.idEstudante());
    }

    @Override
    protected void validar(Dados.Estudante d) {
        exigir(d.idEstudante(), "idEstudante");
        Pessoa.normalizarCpf(d.cpf());
    }

    /** GANCHO sobrescrito: falha ANTES de criar/armazenar, logo nada e gravado. */
    @Override
    protected void verificarPreRequisitos(Dados.Estudante d) {
        if (relacionamento.consultarPessoa(d.cpf()).isEmpty()) {
            throw new PreRequisitoNaoAtendidoException("Pessoa nao cadastrada: CPF " + d.cpf()
                    + ". Cadastre a pessoa no RH antes do estudante.");
        }
    }

    @Override
    protected Estudante criar(Dados.Estudante d) {
        Pessoa pessoa = relacionamento.consultarPessoa(d.cpf()).orElseThrow();
        return new Estudante(d.idEstudante(), pessoa);
    }

    @Override
    protected void armazenar(Estudante estudante) {
        relacionamento.inserirEstudante(estudante);
    }

    @Override
    protected ResultadoCadastro apresentar(Estudante e, boolean novo) {
        return new ResultadoCadastro(e.getIdEstudante(), novo,
                "Estudante " + e.getIdEstudante() + " - " + e.getNomeCompleto() + " (CPF " + e.getCpfPessoa() + ")");
    }
}
