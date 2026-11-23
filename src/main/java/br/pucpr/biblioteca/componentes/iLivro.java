package br.pucpr.biblioteca.componentes;

import br.pucpr.biblioteca.cadastro.Dados;
import br.pucpr.biblioteca.cadastro.ResultadoCadastro;

import java.util.List;
import java.util.Optional;

/** Interface do componente Biblioteca para o cadastro/consulta de livros (diagrama de componentes). */
public interface iLivro {

    ResultadoCadastro incluirLivro(Dados.Livro dados);

    Optional<FichaLivro> consultarLivro(String idLivro);

    List<FichaLivro> listarLivros();

    /** Visao somente-leitura de um livro para a interface. */
    record FichaLivro(String idLivro, String titulo, String autor, String estado, int numRenovacoes) {
        public String resumo() {
            return idLivro + " | " + titulo + " | " + (autor == null ? "-" : autor) + " | " + estado
                    + (numRenovacoes > 0 ? " | renovacoes: " + numRenovacoes : "");
        }
    }
}
