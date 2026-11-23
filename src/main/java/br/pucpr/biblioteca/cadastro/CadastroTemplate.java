package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.persistencia.UnidadeDeTrabalho;

import java.util.Optional;

/**
 * PADRAO TEMPLATE METHOD - papel: Classe abstrata.
 *
 * <p>{@link #incluir(Object)} e o METODO-MODELO: e {@code final} (nao pode ser sobrescrito) e fixa
 * a ordem dos passos:</p>
 * <ol>
 *   <li>buscar existente  - se ja existe, devolve o cadastro existente (duplicado nao e erro);</li>
 *   <li>validar           - dados obrigatorios/formato;</li>
 *   <li>verificar pre-requisitos - GANCHO (padrao: nada); Estudante/Atendente exigem CPF cadastrado;</li>
 *   <li>criar             - instancia a entidade;</li>
 *   <li>armazenar         - UNICO passo que grava;</li>
 *   <li>apresentar        - monta o resultado para a interface.</li>
 * </ol>
 * <p>Como os passos 2 e 3 acontecem antes do passo 5, uma falha de validacao ou de pre-requisito
 * nunca deixa gravacao parcial. Alem disso tudo roda numa unidade de trabalho: qualquer excecao
 * desfaz o que tiver sido gravado.</p>
 *
 * @param <D> dados de entrada do formulario
 * @param <E> entidade cadastrada
 */
public abstract class CadastroTemplate<D, E> {

    private final UnidadeDeTrabalho unidadeDeTrabalho;

    protected CadastroTemplate(UnidadeDeTrabalho unidadeDeTrabalho) {
        this.unidadeDeTrabalho = unidadeDeTrabalho;
    }

    /** METODO-MODELO. */
    public final ResultadoCadastro incluir(D dados) {
        return unidadeDeTrabalho.executar(() -> {
            Optional<E> existente = buscarExistente(dados);      // 1
            if (existente.isPresent()) {
                return apresentar(existente.get(), false);       // duplicado: devolve o existente
            }
            validar(dados);                                      // 2
            verificarPreRequisitos(dados);                       // 3 (gancho)
            E entidade = criar(dados);                           // 4
            armazenar(entidade);                                 // 5
            return apresentar(entidade, true);                   // 6
        });
    }

    // ---------------- passos variaveis ----------------

    protected abstract Optional<E> buscarExistente(D dados);

    protected abstract void validar(D dados);

    /** GANCHO: por padrao nao ha pre-requisitos. Subclasses sobrescrevem se precisarem. */
    protected void verificarPreRequisitos(D dados) {
    }

    protected abstract E criar(D dados);

    protected abstract void armazenar(E entidade);

    protected abstract ResultadoCadastro apresentar(E entidade, boolean novo);

    // ---------------- utilitario para validar ----------------

    protected static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new br.pucpr.biblioteca.dominio.RegraDeNegocioException("Campo obrigatorio: " + campo);
        }
    }
}
