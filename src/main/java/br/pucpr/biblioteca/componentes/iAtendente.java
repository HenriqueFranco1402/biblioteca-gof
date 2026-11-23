package br.pucpr.biblioteca.componentes;

import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;

import java.util.Optional;

/** Interface do componente RH para atendentes (diagrama de componentes). */
public interface iAtendente {

    ResultadoCadastro incluirAtendente(Dados.Atendente dados);

    Optional<String> consultarAtendente(String idAtendente);
}
